package com.diskin.alon.sonix.catalog.presentation.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.sonix.catalog.presentation.databinding.AlbumTrackBinding
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbumTrack

/**
 * [UiAlbumTrack]s adapter.
 */
class AlbumTracksAdapter(
    private val trackClickListener: (UiAlbumTrack) -> (Unit),
    private val optionsMenuClickListener: (UiAlbumTrack,View) -> (Unit)
    ) : ListAdapter<UiAlbumTrack, AlbumTracksAdapter.AlbumTrackViewHolder>(
    DIFF_CALLBACK
){

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiAlbumTrack>() {

            override fun areItemsTheSame(oldItem: UiAlbumTrack, newItem: UiAlbumTrack): Boolean
                    = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: UiAlbumTrack, newItem: UiAlbumTrack)
                    = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumTrackViewHolder {
        val binding = AlbumTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return AlbumTrackViewHolder(
            binding,
            trackClickListener,
            optionsMenuClickListener)
    }

    override fun onBindViewHolder(holder: AlbumTrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlbumTrackViewHolder(
        private val binding: AlbumTrackBinding,
        trackClickListener: (UiAlbumTrack) -> (Unit),
        optionsMenuClickListener: (UiAlbumTrack,View) -> (Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.trackClickListener = trackClickListener
            binding.optionsClickListener = optionsMenuClickListener
        }

        fun bind(track: UiAlbumTrack) {
            binding.track = track
        }
    }
}