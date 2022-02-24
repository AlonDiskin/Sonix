package com.diskin.alon.sonix.player.featuretesting.scenario.bottom_player

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageButton
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.player.data.PlayerStateCacheImpl
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

class PlaylistRestoredSteps(
    private val playlistProvider: SelectedPlayListProvider,
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: FragmentScenario<PlayerFragment>
    private lateinit var exoPlayer: ExoPlayer
    private val playListSubject = BehaviorSubject.create<SelectedPlaylist>()
    private val lastPlayedTrackIndex = 0
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

        // Prepare idling resource
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        // Clear any prev sp state
        clearSharedPrefs()
    }

    @Given("^user currently listen to tracks playlist$")
    fun user_currently_listen_to_tracks_playlist() {
        // Launch player fragment
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)
        // Get exoPLayer instance from service audio player
        waitForAudioServiceCreation()
        val service = AudioPlaybackService.service!!
        exoPlayer = WhiteBox.getInternalState(service.player,"exoPlayer") as ExoPlayer

        // Set idling resource to idle after ui update of player state change
        EspressoIdlingResource.increment()
        service.player.currentTrack.observeForever {
            if (!EspressoIdlingResource.isIdle()) {
                EspressoIdlingResource.decrement()
            }
        }

        // Start test playlist
        playListSubject.onNext(SelectedPlaylist(lastPlayedTrackIndex,deviceTracks.keys.toList()))
        waitForUiThread()
    }

    @When("^he relaunch the app after pausing track$")
    fun he_relaunch_the_app_after_pausing_track() {
        // Pause track
        onView(withId(R.id.play_pause_button))
            .perform(click())
        waitForStateToBeSaved()

        // Kill app after save state that will happen before ui update to pause state
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .executeShellCommand("am kill com.diskin.alon.sonix.player.featuretesting.test")

        playListSubject.onComplete()
        // Relaunch app
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)
    }

    @Then("^player should restore last played playlist$")
    fun player_should_restore_last_played_playlist() {
        waitForAudioServiceCreation()
        val service = AudioPlaybackService.service!!
        exoPlayer = WhiteBox.getInternalState(service.player,"exoPlayer") as ExoPlayer
        exoPlayer.setThrowsWhenUsingWrongThread(false)

        EspressoIdlingResource.increment()
        service.player.currentTrack.observeForever {
            if (!EspressoIdlingResource.isIdle()) {
                EspressoIdlingResource.decrement()
            }
        }
        waitForUiThread()

        assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri)
            .isEqualTo(deviceTracks.keys.elementAt(lastPlayedTrackIndex))
        assertThat(exoPlayer.isPlaying).isFalse()
        deviceTracks.keys.toList().forEachIndexed { index, uri ->
            assertThat(exoPlayer.getMediaItemAt(index).localConfiguration?.uri)
                .isEqualTo(uri)
        }
    }

    @And("^display last played track in paused state$")
    fun display_last_played_track_in_paused_state() {
        val trackMetadata = deviceTracks[deviceTracks.keys.elementAt(lastPlayedTrackIndex)]!!
        val trackTitle = trackMetadata[0] as String
        val trackArtist = trackMetadata[1] as String

        onView(withId(R.id.player_root))
            .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.track_name))
            .check(ViewAssertions.matches(ViewMatchers.withText(trackTitle)))
        onView(withId(R.id.track_artist))
            .check(ViewAssertions.matches(ViewMatchers.withText(trackArtist)))
        scenario.onFragment{
            val button = it.requireView().findViewById<ImageButton>(R.id.play_pause_button)
            val tag = it.requireContext().getString(R.string.tag_play)
            assertThat(button.tag).isEqualTo(tag)
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

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    private fun waitForAudioServiceCreation() {
        scenario.onFragment {
            while (!it.mediaBrowser.isConnected) {
                Thread.sleep(500)
            }
        }
    }

    private fun waitForStateToBeSaved() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        while (!prefs.contains(PlayerStateCacheImpl.KEY_CACHED_STATE)) {
            Thread.sleep(500)
        }
    }
}