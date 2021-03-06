package com.diskin.alon.sonix.catalog.presentation.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack
import com.diskin.alon.sonix.catalog.presentation.databinding.AudioTrackBinding

/**
 * [UiAudioTrack]s adapter.
 */
class AudioTracksAdapter(
    private val trackClickListener: (UiAudioTrack) -> (Unit),
    private val optionsMenuClickListener: (UiAudioTrack,View) -> (Unit)
    ) : ListAdapter<UiAudioTrack, AudioTracksAdapter.AudioTrackViewHolder>(
    DIFF_CALLBACK
){

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiAudioTrack>() {

            override fun areItemsTheSame(oldItem: UiAudioTrack, newItem: UiAudioTrack): Boolean
                    = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: UiAudioTrack, newItem: UiAudioTrack)
                    = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioTrackViewHolder {
        val binding = AudioTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return AudioTrackViewHolder(
            binding,
            trackClickListener,
            optionsMenuClickListener)
    }

    override fun onBindViewHolder(holder: AudioTrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AudioTrackViewHolder(
        private val binding: AudioTrackBinding,
        trackClickListener: (UiAudioTrack) -> (Unit),
        optionsMenuClickListener: (UiAudioTrack,View) -> (Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.trackClickListener = trackClickListener
            binding.optionsClickListener = optionsMenuClickListener
        }

        fun bind(track: UiAudioTrack) {
            binding.track = track
        }
    }
}