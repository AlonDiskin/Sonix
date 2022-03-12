package com.diskin.alon.sonix.player.infrastructure

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.player.infrastructure.model.TrackMetadata
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioNotificationFactoryTest {

    // Test subject
    private lateinit var factory: AudioNotificationFactory

    @Before
    fun setUp() {
        factory = AudioNotificationFactory(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun createNotificationChannel_WhenCreated() {
        // Given

        // Then
        val channel = NotificationManagerCompat.from(ApplicationProvider.getApplicationContext())
            .getNotificationChannel(AudioNotificationFactory.CHANNEL_ID)

        assertThat(channel).isNotNull()
        assertThat(channel?.name).isEqualTo(AudioNotificationFactory.CHANNEL_NAME)
        assertThat(channel?.description).isEqualTo(AudioNotificationFactory.CHANNEL_DESCRIPTION)
    }

    @Test
    fun buildPausedPlaybackNotificationWithControlsAndMetadata() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val trackMetadata = TrackMetadata("title","artist","album",30000L, Uri.EMPTY)
        val session = MediaSessionCompat(ApplicationProvider.getApplicationContext(),"tag")

        // When
        val notification = factory.buildPausedNotification(trackMetadata, session.sessionToken, mockk())

        // Then
        assertThat(notification.smallIcon.resId).isEqualTo(R.drawable.ic_round_music_note_18)
        assertThat(notification.extras.getString(Notification.EXTRA_TITLE)).isEqualTo(trackMetadata.name)
        assertThat(notification.extras.getString(Notification.EXTRA_TEXT)).isEqualTo(trackMetadata.artist)
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
        assertThat(notification.actions[1].getIcon().resId).isEqualTo(R.drawable.ic_round_play_arrow_32)
        assertThat(notification.actions[1].title).isEqualTo(context.getString(R.string.title_notification_play))
        assertThat(notification.actions[1].actionIntent)
            .isEqualTo(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_PLAY
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
    }

    @Test
    fun buildPlayedPlaybackNotificationWithControlsAndMetadata() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()
        val trackMetadata = TrackMetadata("title","artist","album",30000L, Uri.EMPTY)
        val session = MediaSessionCompat(ApplicationProvider.getApplicationContext(),"tag")

        // When
        val notification = factory.buildPlayedNotification(trackMetadata, session.sessionToken,mockk())

        // Then
        assertThat(notification.smallIcon.resId).isEqualTo(R.drawable.ic_round_music_note_18)
        assertThat(notification.extras.getString(Notification.EXTRA_TITLE)).isEqualTo(trackMetadata.name)
        assertThat(notification.extras.getString(Notification.EXTRA_TEXT)).isEqualTo(trackMetadata.artist)
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
        assertThat(notification.actions[1].getIcon().resId).isEqualTo(R.drawable.ic_round_pause_32)
        assertThat(notification.actions[1].title).isEqualTo(context.getString(R.string.title_notification_pause))
        assertThat(notification.actions[1].actionIntent)
            .isEqualTo(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_PAUSE
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
    }

    @Test
    fun navigateToAppPlayer_WhenClicked() {
        // Given
        val contentIntent = mockk<PendingIntent>()
        val trackMetadata = TrackMetadata("title","artist","album",30000L, Uri.EMPTY)
        val session = MediaSessionCompat(ApplicationProvider.getApplicationContext(),"tag")

        // When
        val pausedNotification = factory.buildPausedNotification(trackMetadata, session.sessionToken,contentIntent)
        val playedNotification = factory.buildPlayedNotification(trackMetadata, session.sessionToken,contentIntent)

        // Then
        assertThat(pausedNotification.contentIntent).isEqualTo(contentIntent)
        assertThat(playedNotification.contentIntent).isEqualTo(contentIntent)
    }
}