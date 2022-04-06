package com.diskin.alon.sonix.catalog.presentation.controller

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.databinding.FragmentArtistsBinding
import com.diskin.alon.sonix.catalog.presentation.model.UiArtist
import com.diskin.alon.sonix.catalog.presentation.viewmodel.ArtistsViewModel
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class ArtistsFragment : Fragment() {

    private val viewModel: ArtistsViewModel by viewModels()
    private lateinit var layout: FragmentArtistsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentArtistsBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set artists adapter
        val adapter = ArtistsAdapter(::handleArtistClick)
        layout.artists.adapter = adapter

        // Observe view model artists
        viewModel.artists.observe(viewLifecycleOwner, adapter::submitList)

        // Observe view model artists loading state
        viewModel.update.observe(viewLifecycleOwner, ::handleArtistsLoading)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_artists,menu)
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
                    is ArtistSorting.Name -> menu.findItem(R.id.action_sort_by_artist_name).isChecked =
                        true
                    is ArtistSorting.Date -> menu.findItem(R.id.action_sort_by_date).isChecked =
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
                        R.id.action_sort_by_date ->
                            viewModel.sort(ArtistSorting.Date(viewModel.sorting.value!!.ascending))
                        R.id.action_sort_by_artist_name ->
                            viewModel.sort(ArtistSorting.Name(viewModel.sorting.value!!.ascending))
                    }
                }

                R.id.ordering -> {
                    when(item.itemId) {
                        R.id.action_order_desc ->  {
                            when(viewModel.sorting.value) {
                                is ArtistSorting.Date ->
                                    viewModel.sort(ArtistSorting.Date(false))
                                is ArtistSorting.Name ->
                                    viewModel.sort(ArtistSorting.Name(false))
                            }
                        }
                        R.id.action_order_asc ->  {
                            when(viewModel.sorting.value) {
                                is ArtistSorting.Date ->
                                    viewModel.sort(ArtistSorting.Date(true))
                                is ArtistSorting.Name ->
                                    viewModel.sort(ArtistSorting.Name(true))
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

    private fun handleArtistsLoading(updateState: ViewUpdateState) {
        when(updateState) {
            is ViewUpdateState.Loading -> layout.progressBar.visibility = View.VISIBLE
            is ViewUpdateState.EndLoading -> layout.progressBar.visibility = View.GONE
        }
    }

    private fun handleArtistClick(artist: UiArtist) {
        // TODO open artist detail screen
    }
}