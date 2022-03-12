package com.diskin.alon.sonix.catalog.presentation.controller

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.databinding.FragmentAudioTracksBinding
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AudioTracksViewModel
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        val adapter = AudioTracksAdapter(::handleTrackClick,::handleTrackOptionsClick)
        layout.tracks.adapter = adapter

        // Observe view model tracks
        viewModel.tracks.observe(viewLifecycleOwner, adapter::submitList)

        // Observe view model track error state
        viewModel.error.observe(viewLifecycleOwner, this::handleTracksError)

        // Observe view model tracks loading state
        viewModel.update.observe(viewLifecycleOwner, ::handleTracksUpdate)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tracks,menu)
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
                    is AudioTracksSorting.DateAdded -> menu.findItem(R.id.action_sort_by_date).isChecked =
                        true
                    is AudioTracksSorting.ArtistName -> menu.findItem(R.id.action_sort_by_artist_name).isChecked =
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

    private fun handleTracksError(appError: AppError) {
        when(appError) {
            AppError.DEVICE_STORAGE ->
                Toast.makeText(requireContext(),getString(R.string.error_message_storage),Toast.LENGTH_LONG).show()
            AppError.UNKNOWN_ERROR ->
                Toast.makeText(requireContext(),getString(R.string.error_message_unknown),Toast.LENGTH_LONG).show()
        }
    }

    private fun handleTracksUpdate(updateState: ViewUpdateState) {
        when(updateState) {
            is ViewUpdateState.Loading -> layout.progressBar.visibility = View.VISIBLE
            is ViewUpdateState.EndLoading -> layout.progressBar.visibility = View.GONE
        }
    }

    private fun handleTrackClick(track: UiAudioTrack) {
        val adapter = layout.tracks.adapter as AudioTracksAdapter

        viewModel.playTracks(
            adapter.currentList.indexOf(track),
            adapter.currentList.map { it.id }
        )
    }

    private fun handleTrackOptionsClick(track: UiAudioTrack,view: View) {
        PopupMenu(requireActivity(), view).apply {
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_track_detail -> {
                        showTrackDetail(track.id)
                        true
                    }

                    R.id.action_share_track -> {
                        shareTrack(track.id)
                        true
                    }

                    R.id.action_delete_track -> {
                        deleteTrack(track.id)
                        true
                    }

                    else -> false
                }
            }
            inflate(R.menu.menu_track)
            show()
        }
    }

    private fun showTrackDetail(trackId: Int) {
        val bundle = bundleOf(getString(R.string.arg_track_id) to trackId)
        findNavController().navigate(R.id.audioTrackDetailDialog, bundle)
    }

    private fun shareTrack(trackId: Int) {
        activity?.let {
            val audioCollection =     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val uri = Uri.parse(audioCollection.toString().plus("/$trackId"))

            ShareCompat.IntentBuilder(it)
                .setType(getString(R.string.mime_type_audio))
                .setChooserTitle(getString(R.string.title_share_track))
                .setStream(uri)
                .startChooser()
        }
    }

    private fun deleteTrack(trackId: Int) {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(getString(R.string.message_dialog_delete_track))
            .setTitle(getString(R.string.title_dialog_delete_track))
            .setPositiveButton(getString(R.string.title_dialog_positive_action)) { _, _ ->
                viewModel.deleteTrack(trackId)
            }
            .setNegativeButton(getString(R.string.title_dialog_negative_action), null)
            .create()
            .show()
    }
}