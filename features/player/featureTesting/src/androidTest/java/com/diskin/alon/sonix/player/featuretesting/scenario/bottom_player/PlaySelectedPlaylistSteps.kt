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
import com.google.android.exoplayer2.Player
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class PlaySelectedPlaylistSteps(
    private val playlistProvider: SelectedPlayListProvider,
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: FragmentScenario<PlayerFragment>
    private lateinit var exoPlayer: ExoPlayer
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
    private var selectedTrackIndex: Int = -1

    init {
        every { playlistProvider.get() } returns playListSubject
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Given("^User has 3 tracks on device$")
    fun user_has_tracks_on_device() {
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

    @When("^User open app,and not yet selected playlist to listen to$")
    fun user_open_app_not_yet_selected_playlist_to_listen_to() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)
        // Get exoPLayer instance from service audio player
        waitForAudioServiceCreation()
        val service = AudioPlaybackService.service!!
        exoPlayer = WhiteBox.getInternalState(service.player,"exoPlayer") as ExoPlayer
        exoPlayer.setThrowsWhenUsingWrongThread(false)
    }

    @Then("^Player should be hidden$")
    fun player_should_be_hidden() {
        onView(withId(R.id.player_root))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @When("^User select to play a track at \"([^\"]*)\" from tracks playlist$")
    fun user_select_to_play_a_track_at_something_from_tracks_playlist(position: String) {
        selectedTrackIndex = when(position) {
            "first" -> 0
            "second" -> 1
            "third" -> 2
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
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    EspressoIdlingResource.decrement()
                }
            }
        })
        EspressoIdlingResource.increment()
        playListSubject.onNext(SelectedPlaylist(selectedTrackIndex,deviceTracks.keys.toList()))
        waitForIdlingResource()
    }

    @Then("^Player should start playing selected track and show its metadata$")
    fun player_should_start_playing_selected_track_and_show_metadata() {
        val selectedTrackUri = deviceTracks.keys.elementAt(selectedTrackIndex)

        checkTrackPlaying(selectedTrackUri)
        checkTrackMetadataShown(selectedTrackUri)
    }

    @When("^Track playback completes$")
    fun track_playback_completes() {
        EspressoIdlingResource.increment()
        scenario.onFragment{
            it.requireActivity().runOnUiThread { exoPlayer.seekTo(exoPlayer.duration) }
        }
        waitForIdlingResource()
    }

    @Then("^Player should \"([^\"]*)\" next track in playlist and show its metadata$")
    fun player_should_something_next_track_in_playlist(start: String) {
        when(start) {
            "true" -> {
                val currentExpectedTrack = deviceTracks.keys.elementAt(selectedTrackIndex + 1)

                checkTrackPlaying(currentExpectedTrack)
                checkTrackMetadataShown(currentExpectedTrack)
            }
            "false" -> assertThat(exoPlayer.isPlaying).isFalse()
            else -> throw IllegalStateException("Unknown scenario argument:${start}")
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
}