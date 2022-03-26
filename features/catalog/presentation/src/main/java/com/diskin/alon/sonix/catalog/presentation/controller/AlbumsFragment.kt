package com.diskin.alon.sonix.catalog.presentation.controller

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.databinding.FragmentAlbumsBinding
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbum
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumDetailViewModel
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumsViewModel
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class AlbumsFragment : Fragment() {

    private val viewModel: AlbumsViewModel by viewModels()
    private lateinit var layout: FragmentAlbumsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentAlbumsBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set albums adapter
        val adapter = AlbumsAdapter(::handleAlbumClick)
        layout.albums.adapter = adapter

        // Observe view model albums
        viewModel.albums.observe(viewLifecycleOwner, adapter::submitList)

        // Observe view model tracks loading state
        viewModel.update.observe(viewLifecycleOwner, ::handleAlbumsLoading)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_albums,menu)
        MenuCompat.setGroupDividerEnabled(menu, true)

        // Disable menu while no sorting is available from view model
        menu.findItem(R.id.action_sort).isEnabled = false

        // Observe view model sorting state
        viewModel.sorting.observe(viewLifecycleOwner) {
            it?.let { sorting ->
                menu.findItem(R.id.action_sort).isEnabled = true

                if (sorting.ascending) {
                    menu.findItem(R.id.action_order_asc).isChecked = true
                } else {
                    menu.findItem(R.id.action_order_desc).isChecked = true
                }

                when (sorting) {
                    is AlbumSorting.Artist -> menu.findItem(R.id.action_sort_by_artist_name).isChecked =
                        true
                    is AlbumSorting.Name -> menu.findItem(R.id.action_sort_by_album_name).isChecked =
                        true
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (!item.isChecked && (item.groupId == R.id.sorting || item.groupId == R.id.ordering)) {
            when(item.groupId) {
                R.id.sorting -> {
                    when(item.itemId) {
                        R.id.action_sort_by_album_name ->
                            viewModel.sort(AlbumSorting.Name(viewModel.sorting.value!!.ascending))
                        R.id.action_sort_by_artist_name ->
                            viewModel.sort(AlbumSorting.Artist(viewModel.sorting.value!!.ascending))
                    }
                }

                R.id.ordering -> {
                    when(item.itemId) {
                        R.id.action_order_desc ->  {
                            when(viewModel.sorting.value) {
                                is AlbumSorting.Artist ->
                                    viewModel.sort(AlbumSorting.Artist(false))
                                is AlbumSorting.Name ->
                                    viewModel.sort(AlbumSorting.Name(false))
                            }
                        }
                        R.id.action_order_asc ->  {
                            when(viewModel.sorting.value) {
                                is AlbumSorting.Artist ->
                                    viewModel.sort(AlbumSorting.Artist(true))
                                is AlbumSorting.Name ->
                                    viewModel.sort(AlbumSorting.Name(true))
                            }
                        }
                    }
                }
            }

            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun handleAlbumsLoading(updateState: ViewUpdateState) {
        when(updateState) {
            is ViewUpdateState.Loading -> layout.progressBar.visibility = View.VISIBLE
            is ViewUpdateState.EndLoading -> layout.progressBar.visibility = View.GONE
        }
    }

    private fun handleAlbumClick(album: UiAlbum) {
        openAlbumDetail(album.id)
    }

    private fun openAlbumDetail(id: Int) {
        val bundle = bundleOf(AlbumDetailViewModel.KEY_ALBUM_ID to id)
        findNavController().navigate(R.id.action_catalogFragment_to_albumDetailFragment, bundle)
    }
}