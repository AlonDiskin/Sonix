package com.diskin.alon.sonix.player.presentation

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.player.infrastructure.KEY_SERVICE_ERROR
import com.diskin.alon.sonix.player.infrastructure.model.AudioProgressEvent
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(shadows = [ShadowMediaControllerCompat::class],instrumentedPackages = ["androidx.loader.content"])
class PlayerFragmentTest {

    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var scenario: FragmentScenario<PlayerFragment>

    // Stub data
    private val mediaController: MediaControllerCompat = mockk()
    private val controllerCallbackSlot = slot<MediaControllerCompat.Callback>()

    @Before
    fun setUp() {
        // Set and stub test mocks
        mockkConstructor(MediaControllerCompat::class)
        mockkStatic(MediaControllerCompat::class)
        every { MediaControllerCompat.setMediaController(any(),any()) } returns Unit
        every { anyConstructed<MediaControllerCompat>().registerCallback(capture(controllerCallbackSlot)) } returns Unit
        every { anyConstructed<MediaControllerCompat>().playbackState } returns null
        every { anyConstructed<MediaControllerCompat>().metadata } returns null
        every { MediaControllerCompat.getMediaController(any()) } returns mediaController
        every { mediaController.metadata } returns null

        // Launch fragment under test
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_AppCompat_DayNight)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun connectToMediaServiceAndSetMediaController_WhenCreated() {
        // Given

        // Then
        scenario.onFragment {
            assertThat(it.mediaBrowser.isConnected).isTrue()
            verify { MediaControllerCompat.setMediaController(it.requireActivity(),any()) }
        }
        assertThat(controllerCallbackSlot.isCaptured).isTrue()
    }

    @Test
    fun disconnectFromMediaService_WhenStopped() {
        // Given
        every { MediaControllerCompat.getMediaController(any()) } returns null

        // When
        scenario.onFragment { it.onStop() }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onFragment { assertThat(it.mediaBrowser.isConnected).isFalse() }
    }

    @Test
    fun unregisterMediaControllerCallbacks_WhenStopped() {
        // Given
        every { MediaControllerCompat.getMediaController(any()) } returns mediaController
        every { mediaController.unregisterCallback(any()) } returns Unit

        // When
        scenario.onFragment { it.onStop() }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { mediaController.unregisterCallback(controllerCallbackSlot.captured) }
    }

    @Test
    fun showTrackMetadata_WhenControllerMetadataChange() {
        // Given
        val trackTitle = "title"
        val trackArtist = "artist"
        val trackUri = Uri.EMPTY

        val metaDataBuilder = MediaMetadataCompat.Builder()

        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, trackTitle)
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, trackArtist)
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, trackUri.toString())

        // When
        controllerCallbackSlot.captured.onMetadataChanged(metaDataBuilder.build())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.track_name))
            .check(matches(withText(trackTitle)))
        onView(withId(R.id.track_artist))
            .check(matches(withText(trackArtist)))
        scenario.onFragment{
            val artImageView = it.requireView().findViewById<ImageView>(R.id.track_art)
            assertThat(artImageView.tag).isEqualTo(trackUri)
        }
    }

    @Test
    fun showErrorNotification_WhenPlayerFail() {
        // Given
        val error = AppError.PLAYER_ERROR

        // When
        controllerCallbackSlot.captured.onExtrasChanged(Bundle().apply { putString(KEY_SERVICE_ERROR,error.name) })
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.error_message_player)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }

    @Test
    fun showErrorNotification_WhenDeviceStorageFail() {
        // Given
        val error = AppError.DEVICE_STORAGE

        // When
        controllerCallbackSlot.captured.onExtrasChanged(Bundle().apply { putString(KEY_SERVICE_ERROR,error.name) })
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.error_message_device_storage)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }

    @Test
    fun showErrorNotification_UponUnknownError() {
        // Given
        val error = AppError.UNKNOWN_ERROR

        // When
        controllerCallbackSlot.captured.onExtrasChanged(Bundle().apply { putString(KEY_SERVICE_ERROR,error.name) })
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.error_message_unknown)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }

    @Test
    fun hideLayout_WhenPlayerHasNoTrack() {
        // Given
        val stateBuilder = PlaybackStateCompat.Builder()

        // When
        controllerCallbackSlot.captured.onPlaybackStateChanged(stateBuilder
            .setState(PlaybackStateCompat.STATE_NONE,0,0f).build())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.player_root))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun showLayout_WhenPlayerHasTrack() {
        // Given
        val stateBuilder = PlaybackStateCompat.Builder()

        // When
        controllerCallbackSlot.captured.onPlaybackStateChanged(stateBuilder
            .setState(PlaybackStateCompat.STATE_PLAYING,0,1f).build())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.player_root))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // When
        controllerCallbackSlot.captured.onPlaybackStateChanged(stateBuilder
            .setState(PlaybackStateCompat.STATE_PAUSED,0,1f).build())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.player_root))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun updatePlayPauseButton_WhenTrackPlaybackUpdates() {
        // Given
        val stateBuilder = PlaybackStateCompat.Builder()

        // When
        controllerCallbackSlot.captured.onPlaybackStateChanged(stateBuilder
            .setState(PlaybackStateCompat.STATE_PLAYING,0,0f).build())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onFragment{
            val button = it.requireView().findViewById<ImageButton>(R.id.play_pause_button)
            val tag = it.requireContext().getString(R.string.tag_pause)
            assertThat(button.tag).isEqualTo(tag)
        }

        // When
        controllerCallbackSlot.captured.onPlaybackStateChanged(stateBuilder
            .setState(PlaybackStateCompat.STATE_PAUSED,0,0f).build())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onFragment{
            val button = it.requireView().findViewById<ImageButton>(R.id.play_pause_button)
            val tag = it.requireContext().getString(R.string.tag_play)
            assertThat(button.tag).isEqualTo(tag)
        }
    }

    @Test
    fun pauseTrack_WhenTrackPlayedAndPausedButtonClicked() {
        // Given
        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING,0,0f).build()
        val controls = mockk<MediaControllerCompat.TransportControls>()

        every { mediaController.transportControls } returns controls
        every { mediaController.playbackState } returns playbackState
        every { controls.pause() } returns Unit
        scenario.onFragment {
            it.requireView().findViewById<ConstraintLayout>(R.id.player_root).visibility = View.VISIBLE
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.player_root))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.play_pause_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { mediaController.transportControls }
        verify { controls.pause() }
    }

    @Test
    fun playTrack_WhenTrackPausedAndPlayButtonClicked() {
        // Given
        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PAUSED,0,0f).build()
        val controls = mockk<MediaControllerCompat.TransportControls>()

        every { mediaController.transportControls } returns controls
        every { mediaController.playbackState } returns playbackState
        every { controls.play() } returns Unit
        scenario.onFragment {
            it.requireView().findViewById<ConstraintLayout>(R.id.player_root).visibility = View.VISIBLE
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withId(R.id.player_root))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.play_pause_button))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { mediaController.transportControls }
        verify { controls.play() }
    }

    @Test
    fun updateTrackProgress_WhenProgressUpdates() {
        // Given
        val progress = 45

        scenario.onFragment{
            // When
            it.onAudioProgressEvent(AudioProgressEvent(progress))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            // Then
            val progressBar = it.requireView().findViewById<LinearProgressIndicator>(R.id.progress_bar)

            assertThat(progressBar.progress).isEqualTo(progress)
        }
    }

    @Test
    fun updateTrackProgress_WhenPlaybackUpdates() {
        // Given
        val duration = 60L
        val position = 6L
        val progress = 10
        val metaDataBuilder = MediaMetadataCompat.Builder()
        val stateBuilder = PlaybackStateCompat.Builder()

        metaDataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        every { mediaController.metadata } returns metaDataBuilder.build()

        // When
        controllerCallbackSlot.captured.onPlaybackStateChanged(stateBuilder
            .setState(PlaybackStateCompat.STATE_PLAYING,position,0f).build())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onFragment{
            val progressBar = it.requireView().findViewById<LinearProgressIndicator>(R.id.progress_bar)
            assertThat(progressBar.progress).isEqualTo(progress)
        }
    }
}