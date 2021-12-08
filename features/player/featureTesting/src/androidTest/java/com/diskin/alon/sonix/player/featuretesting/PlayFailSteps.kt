package com.diskin.alon.sonix.player.featuretesting

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.player.featuretesting.util.EspressoIdlingResource
import com.diskin.alon.sonix.player.featuretesting.util.WhiteBox
import com.diskin.alon.sonix.player.infrastructure.AudioPlayer
import com.diskin.alon.sonix.player.presentation.PlayerFragment
import com.diskin.alon.sonix.player.presentation.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.common.truth.Truth
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class PlayFailSteps(
    private val playlistProvider: SelectedPlayListProvider,
    private val contentResolver: ContentResolver,
    player: AudioPlayer
) : GreenCoffeeSteps() {

    private lateinit var scenario: FragmentScenario<PlayerFragment>
    private val playListSubject = BehaviorSubject.create<SelectedPlaylist>()
    private val deviceTracks = mapOf(
        Pair(
            Uri.fromFile(File("//android_asset/audio/track_1.mp3")),
            arrayOf("title_1", "artist_1", "album_1", 12000L)
        ),
        Pair(
            Uri.fromFile(File("//android_asset/audio/track_2.mp3")),
            arrayOf("title_2", "artist_2", "album_2", 12000L)
        ),
        Pair(
            Uri.fromFile(File("//android_asset/audio/track_3.mp3")),
            arrayOf("title_3", "artist_3", "album_3", 12000L)
        )
    )
    private val exoPlayer = WhiteBox.getInternalState(player,"exoPlayer") as ExoPlayer

    init {
        every { playlistProvider.get() } returns playListSubject
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        exoPlayer.setThrowsWhenUsingWrongThread(false)
    }

    @Given("^User has 3 tracks on device$")
    fun user_has_3_tracks_on_device() {
        // Set stub tracks data
        val columns = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        deviceTracks.keys.forEach { uri ->
            every { contentResolver.query(uri,columns,null,null,null)
            } returns createMetadataCursor(deviceTracks[uri])
        }
    }

    @When("^User open app$")
    fun user_open_app() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)
    }

    @And("^User select to play a track at \"([^\"]*)\" from tracks playlist which fail to play$")
    fun user_select_to_play_a_track_at_something_from_tracks_playlist_which_fail(position: String) {
        val uris = mutableListOf<Uri>()
        val selectedTrackIndex = when(position) {
            "first" -> {
                uris.add(Uri.parse(deviceTracks.keys.elementAt(0).toString().plus(".jjjj")))
                uris.add(deviceTracks.keys.elementAt(1))
                uris.add(deviceTracks.keys.elementAt(2))
                0
            }
            "second" -> {
                uris.add(deviceTracks.keys.elementAt(0))
                uris.add(Uri.parse(deviceTracks.keys.elementAt(1).toString().plus(".jjjj")))
                uris.add(deviceTracks.keys.elementAt(2))
                1
            }
            "third" -> {
                uris.add(deviceTracks.keys.elementAt(0))
                uris.add(deviceTracks.keys.elementAt(1))
                uris.add(Uri.parse(deviceTracks.keys.elementAt(2).toString().plus(".jjjj")))
                2
            }
            else -> throw IllegalStateException("Unknown scenario argument:${position}")
        }
        scenario.onFragment {
            MediaControllerCompat.getMediaController(it.requireActivity())?.registerCallback(
                object : MediaControllerCompat.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                        super.onPlaybackStateChanged(state)
                        EspressoIdlingResource.decrement()
                    }
                }
            )
        }
        EspressoIdlingResource.increment()
        playListSubject.onNext(SelectedPlaylist(selectedTrackIndex,uris))
        waitForUiThread()
    }

    @Then("^Player should play \"([^\"]*)\" track and show its metadata$")
    fun player_should_play_something_track_and_show_its_metadata(next: String) {
        when(next) {
            "first" -> {
                val currentExpectedTrack = deviceTracks.keys.elementAt(0)

                checkTrackPlaying(currentExpectedTrack)
                checkTrackMetadataShown(currentExpectedTrack)
            }
            "second" -> {
                val currentExpectedTrack = deviceTracks.keys.elementAt(1)

                checkTrackPlaying(currentExpectedTrack)
                checkTrackMetadataShown(currentExpectedTrack)
            }
            "third" -> {
                val currentExpectedTrack = deviceTracks.keys.elementAt(2)

                checkTrackPlaying(currentExpectedTrack)
                checkTrackMetadataShown(currentExpectedTrack)
            }
            else -> throw IllegalStateException("Unknown scenario argument:${next}")
        }
    }

    private fun createMetadataCursor(values: Array<out Any>?): MatrixCursor {
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION
            ),
            1
        )

        cursor.addRow(values)
        return cursor
    }

    private fun waitForUiThread() {
        while (!EspressoIdlingResource.isIdle()) { Thread.sleep(500) }
    }

    private fun checkTrackPlaying(uri: Uri) {
        Truth.assertThat(exoPlayer.isPlaying).isTrue()
        Truth.assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri).isEqualTo(uri)
    }

    private fun checkTrackMetadataShown(uri: Uri) {
        val trackMetadata = deviceTracks[uri]!!
        val trackTitle = trackMetadata[0] as String
        val trackArtist = trackMetadata[1] as String

        Espresso.onView(ViewMatchers.withId(R.id.player_root))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.track_name))
            .check(ViewAssertions.matches(ViewMatchers.withText(trackTitle)))
        Espresso.onView(ViewMatchers.withId(R.id.track_artist))
            .check(ViewAssertions.matches(ViewMatchers.withText(trackArtist)))
    }
}