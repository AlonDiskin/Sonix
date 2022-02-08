package com.diskin.alon.sonix.player.featuretesting.scenario.bottom_player

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.player.featuretesting.util.EspressoIdlingResource
import com.diskin.alon.sonix.player.featuretesting.util.WhiteBox
import com.diskin.alon.sonix.player.infrastructure.AudioPlaybackService
import com.diskin.alon.sonix.player.presentation.PlayerFragment
import com.diskin.alon.sonix.player.presentation.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class TrackPlayFailSteps(
    private val playlistProvider: SelectedPlayListProvider,
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: FragmentScenario<PlayerFragment>
    private lateinit var exoPlayer: ExoPlayer
    private var selectedIndex = -1
    private val selectedUris = mutableListOf<Uri>()
    private lateinit var expectedPlayedTrackUri: Uri
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

    init {
        // Stub mocked collaborators
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
        every { playlistProvider.get() } returns playListSubject

        // Register idling resource
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Given("^user select to listen to a track positioned at \"([^\"]*)\" in a playlist$")
    fun user_select_to_listen_to_a_track_positioned_at_position_in_a_playlist(position: String) {
        // Launch player fragment
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)

        // Get exoPLayer instance from service audio player
        waitForAudioServiceCreation()
        val service = AudioPlaybackService.service!!
        exoPlayer = WhiteBox.getInternalState(service.player,"exoPlayer") as ExoPlayer
        exoPlayer.setThrowsWhenUsingWrongThread(false)

        // Add listener to media playback change
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

        selectedIndex = when(position) {
            "first" -> {
                selectedUris.add(Uri.parse(deviceTracks.keys.elementAt(0).toString().plus(".jjjj")))
                selectedUris.add(deviceTracks.keys.elementAt(1))
                selectedUris.add(deviceTracks.keys.elementAt(2))
                0
            }
            "last" -> {
                selectedUris.add(deviceTracks.keys.elementAt(0))
                selectedUris.add(deviceTracks.keys.elementAt(1))
                selectedUris.add(Uri.parse(deviceTracks.keys.elementAt(2).toString().plus(".jjjj")))
                2
            }
            else -> throw IllegalStateException("Unknown scenario argument:${position}")
        }
    }

    @When("^player fail to play selected track$")
    fun player_fail_to_play_selected_track() {
        EspressoIdlingResource.increment()
        playListSubject.onNext(SelectedPlaylist(selectedIndex,selectedUris))
        waitForIdlingResource()
    }

    @Then("^player should play \"([^\"]*)\" from playlist$")
    fun player_should_play_next_from_playlist(next: String) {
        expectedPlayedTrackUri = when(next) {
            "first" -> deviceTracks.keys.elementAt(0)
            "second" -> deviceTracks.keys.elementAt(1)
            else -> throw IllegalStateException("Unknown scenario argument:${next}")
        }

        checkTrackPlaying(expectedPlayedTrackUri)
    }

    @And("^display its metadata$")
    fun display_its_metadata() {
        checkTrackMetadataShown(expectedPlayedTrackUri)
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

    private fun waitForIdlingResource() {
        while (!EspressoIdlingResource.isIdle()) { Thread.sleep(500) }
    }

    private fun waitForAudioServiceCreation() {
        scenario.onFragment {
            while (!it.mediaBrowser.isConnected) {
                Thread.sleep(500)
            }
        }
    }

    private fun checkTrackPlaying(uri: Uri) {
        assertThat(exoPlayer.isPlaying).isTrue()
        assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri).isEqualTo(uri)
    }

    private fun checkTrackMetadataShown(uri: Uri) {
        val trackMetadata = deviceTracks[uri]!!
        val trackTitle = trackMetadata[0] as String
        val trackArtist = trackMetadata[1] as String

        onView(withId(R.id.player_root))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.track_name))
            .check(matches(withText(trackTitle)))
        onView(withId(R.id.track_artist))
            .check(matches(withText(trackArtist)))
    }
}