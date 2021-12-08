package com.diskin.alon.sonix.player.presentation

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.player.infrastructure.AudioPlaybackService
import com.diskin.alon.sonix.player.infrastructure.KEY_SERVICE_ERROR
import com.diskin.alon.sonix.player.infrastructure.model.AudioProgressEvent
import com.diskin.alon.sonix.player.presentation.databinding.FragmentPlayerBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PlayerFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "PlayerFragment"
    }

    private lateinit var layoutBinding: FragmentPlayerBinding
    @VisibleForTesting
    lateinit var mediaBrowser: MediaBrowserCompat
    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->
                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    requireContext(), // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(requireActivity(), mediaController)

                // Check if has current state to present
                mediaController.playbackState?.let { controllerCallback.onPlaybackStateChanged(it) }
                mediaController.metadata?.let { controllerCallback.onMetadataChanged(it) }

                // Register a Callback to stay in sync
                mediaController.registerCallback(controllerCallback)
            }
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            Log.d(LOG_TAG,"media browser connection: onConnectionSuspended")
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
            Log.d(LOG_TAG,"media browser connection: onConnectionFailed")
        }
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            layoutBinding.track = UiPlayerTrack(
                metadata.bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE)!!,
                metadata.bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)!!,
                Uri.parse(metadata.bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)!!)
            )
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            when(state.state) {
                PlaybackStateCompat.STATE_NONE -> layoutBinding.root.visibility = View.GONE
                PlaybackStateCompat.STATE_PLAYING -> {
                    layoutBinding.playPauseButton.setImageResource(R.drawable.ic_round_pause_32)
                    layoutBinding.playPauseButton.tag = getString(R.string.tag_pause)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    layoutBinding.playPauseButton.setImageResource(R.drawable.ic_round_play_arrow_32)
                    layoutBinding.playPauseButton.tag = getString(R.string.tag_play)
                }
            }

            if (state.state == PlaybackStateCompat.STATE_PAUSED || state.state == PlaybackStateCompat.STATE_PLAYING) {
                layoutBinding.root.visibility = View.VISIBLE
            }
        }

        override fun onExtrasChanged(extras: Bundle) {
            extras.getString(KEY_SERVICE_ERROR)?.let { error ->
                when(AppError.valueOf(error)) {
                    AppError.DEVICE_STORAGE -> notifyDeviceStorageError()
                    AppError.PLAYER_ERROR -> notifyPlayerError()
                    AppError.UNKNOWN_ERROR -> notifyUnknownError()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create MediaBrowserServiceCompat
        mediaBrowser = MediaBrowserCompat(
            requireContext(),
            ComponentName(requireContext(), AudioPlaybackService::class.java),
            connectionCallbacks,
            null // optional Bundle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layoutBinding = FragmentPlayerBinding.inflate(inflater,container,false)
        return layoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        layoutBinding.playPauseButton.setOnClickListener {
            MediaControllerCompat.getMediaController(requireActivity())?.let { controller ->
                controller.playbackState?.let { playbackState ->
                    when(playbackState.state) {
                        PlaybackStateCompat.STATE_PAUSED -> controller.transportControls.play()
                        PlaybackStateCompat.STATE_PLAYING -> controller.transportControls.pause()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(requireActivity())?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
        EventBus.getDefault().unregister(this)
        layoutBinding.playerRoot.visibility = View.GONE
    }

    private fun notifyUnknownError() {
        Toast.makeText(requireContext(),getString(R.string.error_message_unknown), Toast.LENGTH_LONG).show()
    }

    private fun notifyPlayerError() {
        Toast.makeText(requireContext(),getString(R.string.error_message_player), Toast.LENGTH_LONG).show()
    }

    private fun notifyDeviceStorageError() {
        Toast.makeText(requireContext(),getString(R.string.error_message_device_storage), Toast.LENGTH_LONG).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAudioProgressEvent(event: AudioProgressEvent) {
        layoutBinding.progressBar.progress = event.progress
    }
}