package com.diskin.alon.sonix.catalog.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.sonix.catalog.presentation.databinding.ArtistBinding
import com.diskin.alon.sonix.catalog.presentation.model.UiArtist

/**
 * [UiArtist]s adapter.
 */
class ArtistsAdapter(
    private val artistClickListener: (UiArtist) -> (Unit)
) : ListAdapter<UiArtist, ArtistsAdapter.ArtistViewHolder>(
    DIFF_CALLBACK
){

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiArtist>() {

            override fun areItemsTheSame(oldItem: UiArtist, newItem: UiArtist): Boolean
                    = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: UiArtist, newItem: UiArtist)
                    = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return ArtistViewHolder(binding,artistClickListener)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ArtistViewHolder(
        private val binding: ArtistBinding,
        artistClickListener: (UiArtist) -> (Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.albumClickListener = artistClickListener
        }

        fun bind(artist: UiArtist) {
            binding.artist = artist
        }
    }
}