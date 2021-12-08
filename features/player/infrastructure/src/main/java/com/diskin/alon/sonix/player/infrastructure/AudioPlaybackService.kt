package com.diskin.alon.sonix.player.infrastructure

import android.app.PendingIntent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.VisibleForTesting
import androidx.media.MediaBrowserServiceCompat
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.player.infrastructure.interfaces.TrackMetadataStore
import com.diskin.alon.sonix.player.infrastructure.model.AudioPlayerTrack
import com.diskin.alon.sonix.player.infrastructure.model.TrackMetadata
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
const val LOG_TAG = "AudioPlaybackService"
const val KEY_SERVICE_ERROR = "audio_service_error"

@AndroidEntryPoint
class AudioPlaybackService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var playListProvider: SelectedPlayListProvider
    @Inject
    lateinit var player: AudioPlayer
    @Inject
    lateinit var metadataStore: TrackMetadataStore
    private val audioTrackSubject = BehaviorSubject.create<AudioPlayerTrack>()
    @VisibleForTesting
    lateinit var mediaSession: MediaSessionCompat
    @VisibleForTesting
    val sessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            player.play()
        }

        override fun onPause() {
            player.pause()
        }

        override fun onStop() {
        }

        override fun onSkipToNext() {
        }

        override fun onSkipToPrevious() {
        }

        override fun onSeekTo(pos: Long) {
        }
    }
    @VisibleForTesting
    val disposable = CompositeDisposable(createAudioPlayerTrackSubscription())

    override fun onCreate() {
        super.onCreate()
        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {
            // Set a pending intent for session activity
            setSessionActivity(
                packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                    PendingIntent.getActivity(applicationContext, 0, sessionIntent, 0)
                }
            )

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(sessionCallback)

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }

        // Subscribe to playlist selections
        disposable.add(createPlayListSubscription())

        // Observe player track state
        player.currentTrack.observeForever { track ->
            // Check if player update for track or empty track(null)
            track?.let {
                // Check if player track is same as currently in subject
                if (track.uri != audioTrackSubject.value?.uri) {
                    // Not same tracks,update subject to trigger fresh metadata update
                    audioTrackSubject.onNext(track)
                } else {
                    // Same track,no need to reload metadata,just update playback state
                    updatePlaybackState(track)
                }
            } ?: run {
                updateServiceHasNoTrack()
            }
        }

        // Observer player error state
        player.error.observeForever(::handlePlayerError)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        throw IllegalStateException("operation not supported!" +
                ",check MediaBrowserServiceCompat#onGetRoot() is implemented correctly ")
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
    }

    private fun createPlayListSubscription(): Disposable {
        return playListProvider.get()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ player.playTracks(it.startIndex,it.tracks) },
                Throwable::printStackTrace)
    }

    private fun handlePlayerError(error: AppError) {
        updateServiceErrorToSessionObservers(error)
    }

    private fun updateSessionMetaData(metadata: TrackMetadata) {
        val metaDataBuilder = MediaMetadataCompat.Builder()

        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata.name)
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.artist)
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadata.album)
        metaDataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, metadata.duration)
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, metadata.uri.toString())

        mediaSession.setMetadata(metaDataBuilder.build())
    }

    private fun updatePlaybackState(track: AudioPlayerTrack) {
        val stateBuilder = PlaybackStateCompat.Builder()

        stateBuilder.setState(
            when(track.isPlaying) {
                false -> PlaybackStateCompat.STATE_PAUSED
                true -> PlaybackStateCompat.STATE_PLAYING
            },
            0,
            1.0f
        )
        stateBuilder.setActions(
            PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_STOP
        )
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun updateServiceHasNoTrack() {
        val metaDataBuilder = MediaMetadataCompat.Builder()
        val stateBuilder = PlaybackStateCompat.Builder()

        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,"")
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,"")
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,"")
        metaDataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,0)
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,Uri.EMPTY.toString())

        stateBuilder.setState(PlaybackStateCompat.STATE_NONE,0,0f)

        mediaSession.setMetadata(metaDataBuilder.build())
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun createAudioPlayerTrackSubscription(): Disposable {
        return audioTrackSubject.switchMapSingle { track ->
            metadataStore.get(track.uri)
                .map { metadataRes -> Pair(track,metadataRes) }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleTrackMetadataStoreResult,::handleAudioPlayerTrackSubscriptionFail)
    }

    private fun handleTrackMetadataStoreResult(pair: Pair<AudioPlayerTrack,AppResult<TrackMetadata>>) {
        when(val result = pair.second) {
            is AppResult.Success -> {
                updateSessionMetaData(result.data)
                updatePlaybackState(pair.first)
            }
            is AppResult.Error -> updateServiceErrorToSessionObservers(result.error)
        }
    }

    private fun handleAudioPlayerTrackSubscriptionFail(error: Throwable) {
        updateServiceErrorToSessionObservers(AppError.INTERNAL_ERROR)
    }

    private fun updateServiceErrorToSessionObservers(error: AppError) {
        mediaSession.setExtras(Bundle().apply { putString(KEY_SERVICE_ERROR,error.name) })
    }
}