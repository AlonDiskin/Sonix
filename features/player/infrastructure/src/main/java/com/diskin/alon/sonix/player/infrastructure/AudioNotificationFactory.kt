package com.diskin.alon.sonix.player.infrastructure

import android.app.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.diskin.alon.sonix.player.infrastructure.model.TrackMetadata
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioNotificationFactory @Inject constructor(
    private val app: Application
) {

    companion object {
        const val CHANNEL_ID = "sonix player notification channel id"
        const val CHANNEL_NAME = "sonix player notification channel"
        const val CHANNEL_DESCRIPTION = "sonix player notification"
    }

    private val defaultArt = BitmapFactory.decodeResource(app.resources,R.drawable.default_art)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = app.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildPausedNotification(
        metadata: TrackMetadata,
        sessionToken: MediaSessionCompat.Token,
        contentIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(app, CHANNEL_ID).apply {
            // Add the metadata for the currently playing track
            setContentTitle(metadata.name)
            setContentText(metadata.artist)
            setLargeIcon(getTrackArt(metadata.uri))

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Enable launching the player by clicking the notification
            setContentIntent(contentIntent)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    app,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Hide time
            setShowWhen(false)

            // Add an app icon
            setSmallIcon(R.drawable.ic_round_music_note_18)

            // Add buttons
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_previous_32,
                    app.getString(R.string.title_notification_skip_prev),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        app,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_play_arrow_32,
                    app.getString(R.string.title_notification_play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        app,
                        PlaybackStateCompat.ACTION_PLAY
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_next_32,
                    app.getString(R.string.title_notification_skip_next),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        app,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )

            // Take advantage of MediaStyle features
            setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(0,1,2)

                // Add a cancel button
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        app,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
            )
        }.build()
    }

    fun buildPlayedNotification(
        metadata: TrackMetadata,
        sessionToken: MediaSessionCompat.Token,
        contentIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(app, CHANNEL_ID).apply {
            // Add the metadata for the currently playing track
            setContentTitle(metadata.name)
            setContentText(metadata.artist)
            setLargeIcon(getTrackArt(metadata.uri))

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Enable launching the player by clicking the notification
            setContentIntent(contentIntent)

            // Hide time
            setShowWhen(false)

            // Add an app icon
            setSmallIcon(R.drawable.ic_round_music_note_18)

            // Add buttons
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_previous_32,
                    app.getString(R.string.title_notification_skip_prev),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        app,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_pause_32,
                    app.getString(R.string.title_notification_pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        app,
                        PlaybackStateCompat.ACTION_PAUSE
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_round_skip_next_32,
                    app.getString(R.string.title_notification_skip_next),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        app,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )

            // Take advantage of MediaStyle features
            setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(0,1,2)

                // Add a cancel button
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        app,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
            )
        }.build()
    }

    private fun getTrackArt(uri: Uri): Bitmap {
        val mr = MediaMetadataRetriever()
        return try {
            mr.setDataSource(app,uri)
            val bitmap = mr.embeddedPicture?.let { byteArr ->
                val inputStream = ByteArrayInputStream(byteArr)
                BitmapFactory.decodeStream(inputStream)
            } ?: defaultArt

            bitmap
        } catch (error: Exception) {
            defaultArt
        } finally {
            mr.close()
        }
    }
}