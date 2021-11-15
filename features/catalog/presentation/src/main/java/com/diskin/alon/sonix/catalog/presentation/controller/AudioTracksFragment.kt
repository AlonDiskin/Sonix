package com.diskin.alon.sonix.catalog.presentation.controller

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.databinding.FragmentAudioTracksBinding
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AudioTracksViewModel
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class AudioTracksFragment : Fragment() {

    private val viewModel: AudioTracksViewModel by viewModels()
    private lateinit var layout: FragmentAudioTracksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentAudioTracksBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set tracks adapter
        val adapter = AudioTracksAdapter()
        layout.tracks.adapter = adapter

        // Observe view model tracks
        viewModel.tracks.observe(viewLifecycleOwner) {
            adapter.submitList(it) {
                layout.tracks.scrollToPosition(0)
            }
        }

        // Observe view model track error state
        viewModel.error.observe(viewLifecycleOwner) {
            handleTracksError(it)
        }

        // Observe view model tracks loading state
        viewModel.update.observe(viewLifecycleOwner, ::handleTracksUpdate)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tracks,menu)
        MenuCompat.setGroupDividerEnabled(menu, true)

        // Disable menu while no sorting is available from view model
        menu.findItem(R.id.action_sort).isEnabled = false

        // Observe view model sorting state
        viewModel.sorting.observe(viewLifecycleOwner, {
            it?.let { sorting ->
                menu.findItem(R.id.action_sort).isEnabled = true

                if (sorting.ascending) {
                    menu.findItem(R.id.action_order_asc).isChecked = true
                } else {
                    menu.findItem(R.id.action_order_desc).isChecked = true
                }

                when(sorting) {
                    is AudioTracksSorting.DateAdded -> menu.findItem(R.id.action_sort_by_date).isChecked = true
                    is AudioTracksSorting.ArtistName -> menu.findItem(R.id.action_sort_by_artist_name).isChecked = true
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (!item.isChecked && (item.groupId == R.id.sorting || item.groupId == R.id.ordering)) {
            when(item.groupId) {
                R.id.sorting -> {
                    when(item.itemId) {
                        R.id.action_sort_by_date ->
                            viewModel.sortTracks(AudioTracksSorting.DateAdded(viewModel.sorting.value!!.ascending))
                        R.id.action_sort_by_artist_name ->
                            viewModel.sortTracks(AudioTracksSorting.ArtistName(viewModel.sorting.value!!.ascending))
                    }
                }

                R.id.ordering -> {
                    when(item.itemId) {
                        R.id.action_order_desc ->  {
                            when(viewModel.sorting.value) {
                                is AudioTracksSorting.DateAdded ->
                                    viewModel.sortTracks(AudioTracksSorting.DateAdded(false))
                                is AudioTracksSorting.ArtistName ->
                                    viewModel.sortTracks(AudioTracksSorting.ArtistName(false))
                            }
                        }
                        R.id.action_order_asc ->  {
                            when(viewModel.sorting.value) {
                                is AudioTracksSorting.DateAdded ->
                                    viewModel.sortTracks(AudioTracksSorting.DateAdded(true))
                                is AudioTracksSorting.ArtistName ->
                                    viewModel.sortTracks(AudioTracksSorting.ArtistName(true))
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

    private fun handleTracksError(appError: com.diskin.alon.sonix.catalog.application.util.AppError) {
        when(appError) {
            com.diskin.alon.sonix.catalog.application.util.AppError.DEVICE_STORAGE ->
                Toast.makeText(requireContext(),getString(R.string.error_message_storage),Toast.LENGTH_LONG).show()
            com.diskin.alon.sonix.catalog.application.util.AppError.UNKNOWN_ERROR ->
                Toast.makeText(requireContext(),getString(R.string.error_message_unknown),Toast.LENGTH_LONG).show()
        }
    }

    private fun handleTracksUpdate(updateState: ViewUpdateState) {
        when(updateState) {
            is ViewUpdateState.Loading -> layout.progressBar.visibility = View.VISIBLE
            is ViewUpdateState.EndLoading -> layout.progressBar.visibility = View.GONE
        }
    }
}