package com.diskin.alon.sonix.catalog.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.sonix.catalog.presentation.databinding.AlbumBinding
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbum

/**
 * [UiAlbum]s adapter.
 */
class AlbumsAdapter : ListAdapter<UiAlbum, AlbumsAdapter.AlbumViewHolder>(
    DIFF_CALLBACK
){

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiAlbum>() {

            override fun areItemsTheSame(oldItem: UiAlbum, newItem: UiAlbum): Boolean
                    = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: UiAlbum, newItem: UiAlbum)
                    = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = AlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlbumViewHolder(
        private val binding: AlbumBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: UiAlbum) {
            binding.album = album
        }
    }
}