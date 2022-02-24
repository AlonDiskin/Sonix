package com.diskin.alon.sonix.player.playerfeaturetest.scenario.notification_player

import android.app.Notification
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.media.session.MediaButtonReceiver
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.player.infrastructure.AudioPlaybackService
import com.diskin.alon.sonix.player.infrastructure.NOTIFICATION_ID
import com.diskin.alon.sonix.player.playerfeaturetest.util.EspressoIdlingResource
import com.diskin.alon.sonix.player.playerfeaturetest.util.WhiteBox
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

class PlaybackControlledSteps(
    private val playlistProvider: SelectedPlayListProvider,
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val context = ApplicationProvider.getApplicationContext<Context>()
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
    private val selectedTrackIndex: Int = 1

    init {
        val columns = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )

        every { playlistProvider.get() } returns playListSubject
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        deviceTracks.keys.forEach { uri ->
            every { contentResolver.query(uri,columns,null,null,null)
            } returns createMetadataCursor(deviceTracks[uri])
        }

        clearSharedPrefs()
    }

    @Given("^user has selected a playlist from app$")
    fun user_has_selected_a_playlist_from_app() {
        // Launch player fragment
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)

        // Get exoPLayer instance from service audio player
        waitForAudioServiceCreation()
        val service = AudioPlaybackService.service!!
        exoPlayer = WhiteBox.getInternalState(service.player,"exoPlayer") as ExoPlayer
        exoPlayer.setThrowsWhenUsingWrongThread(false)

        // Add listener to player to monitor idling resource and select track to start playback
        service.player.currentTrack.observeForever {
            if (!EspressoIdlingResource.isIdle()) {
                EspressoIdlingResource.decrement()
            }
        }
        EspressoIdlingResource.increment()
        playListSubject.onNext(SelectedPlaylist(selectedTrackIndex, deviceTracks.keys.toList()))
        waitForIdlingResource()
    }

    @When("^he \"([^\"]*)\" track via notification$")
    fun he_something_track_via_notification(action: String) {
        device.openNotification()
        Thread.sleep(2000)

        when(action) {
            "pause" -> device.findObject(UiSelector()
                .description(context.getString(R.string.title_notification_pause)))
                .click()

            "skip next" -> device.findObject(UiSelector()
                .description(context.getString(R.string.title_notification_skip_next)))
                .click()

            "skip prev" -> device.findObject(UiSelector()
                .description(context.getString(R.string.title_notification_skip_prev)))
                .click()

            else -> throw IllegalArgumentException("Unknown scenario argument:$action")
        }

        Thread.sleep(2000)
    }

    @Then("^player should \"([^\"]*)\"$")
    fun player_should_something(outcome: String) {
        when(outcome) {
            "pause current track" -> {
                assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri)
                    .isEqualTo(deviceTracks.keys.elementAt(selectedTrackIndex))
                assertThat(exoPlayer.isPlaying).isFalse()
            }

            "play next track" -> {
                assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri)
                    .isEqualTo(deviceTracks.keys.elementAt(selectedTrackIndex + 1))
                assertThat(exoPlayer.isPlaying).isTrue()
            }

            "play prev track" -> {
                assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri)
                    .isEqualTo(deviceTracks.keys.elementAt(selectedTrackIndex - 1))
                assertThat(exoPlayer.isPlaying).isTrue()
            }

            else -> throw IllegalArgumentException("Unknown scenario argument:$outcome")
        }
    }

    @And("^update notification ui according to \"([^\"]*)\"$")
    fun update_notification_ui_accordingly(outcome: String) {
        val notificationManager: NotificationManager = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = notificationManager.activeNotifications
            .find { it.id == NOTIFICATION_ID }!!.notification

        assertThat(notification.smallIcon.resId).isEqualTo(R.drawable.ic_round_music_note_18)
        assertThat(notification.actions.size).isEqualTo(3)
        assertThat(notification.actions[0].getIcon().resId).isEqualTo(R.drawable.ic_round_skip_previous_32)
        assertThat(notification.actions[0].title).isEqualTo(context.getString(R.string.title_notification_skip_prev))
        assertThat(notification.actions[0].actionIntent)
            .isEqualTo(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
        assertThat(notification.actions[2].getIcon().resId).isEqualTo(R.drawable.ic_round_skip_next_32)
        assertThat(notification.actions[2].title).isEqualTo(context.getString(R.string.title_notification_skip_next))
        assertThat(notification.actions[2].actionIntent)
            .isEqualTo(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )

        when(outcome) {
            "pause current track" -> {
                val trackName = deviceTracks.values.elementAt(selectedTrackIndex)[0]
                val trackArtist = deviceTracks.values.elementAt(selectedTrackIndex)[1]

                assertThat(notification.extras.getString(Notification.EXTRA_TITLE)).isEqualTo(trackName)
                assertThat(notification.extras.getString(Notification.EXTRA_TEXT)).isEqualTo(trackArtist)
                assertThat(notification.actions[1].getIcon().resId).isEqualTo(R.drawable.ic_round_play_arrow_32)
                assertThat(notification.actions[1].title).isEqualTo(context.getString(R.string.title_notification_play))
                assertThat(notification.actions[1].actionIntent)
                    .isEqualTo(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PLAY
                        )
                    )
            }

            "play next track" -> {
                val trackName = deviceTracks.values.elementAt(selectedTrackIndex + 1)[0]
                val trackArtist = deviceTracks.values.elementAt(selectedTrackIndex + 1)[1]

                assertThat(notification.extras.getString(Notification.EXTRA_TITLE)).isEqualTo(trackName)
                assertThat(notification.extras.getString(Notification.EXTRA_TEXT)).isEqualTo(trackArtist)
                assertThat(notification.actions[1].getIcon().resId).isEqualTo(R.drawable.ic_round_pause_32)
                assertThat(notification.actions[1].title).isEqualTo(context.getString(R.string.title_notification_pause))
                assertThat(notification.actions[1].actionIntent)
                    .isEqualTo(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PAUSE
                        )
                    )
            }

            "play prev track" -> {
                val trackName = deviceTracks.values.elementAt(selectedTrackIndex - 1)[0]
                val trackArtist = deviceTracks.values.elementAt(selectedTrackIndex - 1)[1]

                assertThat(notification.extras.getString(Notification.EXTRA_TITLE)).isEqualTo(trackName)
                assertThat(notification.extras.getString(Notification.EXTRA_TEXT)).isEqualTo(trackArtist)
                assertThat(notification.actions[1].getIcon().resId).isEqualTo(R.drawable.ic_round_pause_32)
                assertThat(notification.actions[1].title).isEqualTo(context.getString(R.string.title_notification_pause))
                assertThat(notification.actions[1].actionIntent)
                    .isEqualTo(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PAUSE
                        )
                    )
            }

            else -> throw IllegalArgumentException("Unknown scenario argument:$outcome")
        }

        device.pressBack()
        device.pressBack()
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

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}