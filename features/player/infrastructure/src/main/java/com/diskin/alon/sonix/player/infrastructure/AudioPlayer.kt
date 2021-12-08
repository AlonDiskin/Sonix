package com.diskin.alon.sonix.player.infrastructure

import android.app.Application
import android.net.Uri
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.player.infrastructure.model.AudioPlayerTrack
import com.diskin.alon.sonix.player.infrastructure.model.AudioProgressEvent
import com.diskin.alon.sonix.player.infrastructure.model.SingleLiveEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy
import dagger.hilt.android.scopes.ServiceScoped
import org.greenrobot.eventbus.EventBus
import java.io.FileNotFoundException
import javax.inject.Inject

/**
 * Audio player for playing audio tracks from users device.
 */
@ServiceScoped
class AudioPlayer @Inject constructor(app: Application) {

    private val exoPlayer = ExoPlayer.Builder(app)
        .setMediaSourceFactory(createCustomMediaSourceFactory(app))
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        .build().also { it.addListener(createExoPlayerListener()) }
    private val _currentTrack = MutableLiveData<AudioPlayerTrack?>()
    val currentTrack: LiveData<AudioPlayerTrack?> get() = _currentTrack
    val error = SingleLiveEvent<AppError>()
    private val progressUpdater = AudioProgressUpdater(exoPlayer)
    { EventBus.getDefault().post(AudioProgressEvent(it)) }

    @MainThread
    fun playTracks(startIndex: Int, tracks: List<Uri>) {
        if (tracks.isNotEmpty()) {
            if (tracks == getTracksUri()) {
                exoPlayer.seekTo(startIndex,0)
                exoPlayer.play()
            } else {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                tracks.forEach { uri -> exoPlayer.addMediaItem(MediaItem.fromUri(uri)) }
                exoPlayer.prepare()
                exoPlayer.seekTo(startIndex,0)
                exoPlayer.play()
            }
        }
    }

    @MainThread
    fun release() {
        exoPlayer.release()
        progressUpdater.release()
    }

    @MainThread
    fun play() {
        exoPlayer.play()
    }

    @MainThread
    fun pause() {
        exoPlayer.pause()
    }

    private fun getTracksUri(): List<Uri> {
        val uris = mutableListOf<Uri>()

        if (exoPlayer.mediaItemCount > 0) {
            for (i in 0 until exoPlayer.mediaItemCount) {
                exoPlayer.getMediaItemAt(i).localConfiguration?.let {
                    uris.add(it.uri)
                } ?: run {
                    error.value = AppError.INTERNAL_ERROR
                }
            }
        }

        return uris
    }

    private fun updatePlayerTrackAudio() {
        if (exoPlayer.mediaItemCount > 0) {
            exoPlayer.currentMediaItem?.localConfiguration?.let {
                val update = AudioPlayerTrack(it.uri,exoPlayer.isPlaying)
                if (_currentTrack.value != update) _currentTrack.value = update
            } ?: run {
                error.value = AppError.INTERNAL_ERROR
            }
        }
    }

    private fun createCustomMediaSourceFactory(app: Application): DefaultMediaSourceFactory {
        return DefaultMediaSourceFactory(app)
            .setLoadErrorHandlingPolicy(object : DefaultLoadErrorHandlingPolicy() {

                override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
                    return if (loadErrorInfo.exception is FileNotFoundException){
                        C.TIME_UNSET
                    } else {
                        super.getRetryDelayMsFor(loadErrorInfo)
                    }
                }

                override fun getMinimumLoadableRetryCount(dataType: Int): Int {
                    return 0
                }
            })
    }

    private fun createExoPlayerListener():  Player.Listener {
        return object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // For play/pause detection
                if (exoPlayer.playbackState == Player.STATE_READY) {
                    updatePlayerTrackAudio()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                // For skip next/skip prev detection(state changes after BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    updatePlayerTrackAudio()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                // For auto track changes upon completion detection
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    updatePlayerTrackAudio()
                }
            }

            override fun onPlayerError(playerError: PlaybackException) {
                when(playerError.errorCode) {
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> {
                        exoPlayer.removeMediaItem(exoPlayer.currentMediaItemIndex)
                        when (exoPlayer.mediaItemCount) {
                            0 -> _currentTrack.value = null
                            else -> exoPlayer.prepare()
                        }
                    }
                    else -> error.value = AppError.INTERNAL_ERROR
                }
            }
        }
    }
}