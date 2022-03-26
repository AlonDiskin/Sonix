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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.databinding.FragmentAlbumDetailBinding
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbumTrack
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumDetailViewModel
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class AlbumDetailFragment : Fragment() {

    private val viewModel: AlbumDetailViewModel by viewModels()
    private lateinit var layout: FragmentAlbumDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentAlbumDetailBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set tracks adapter
        val adapter = AlbumTracksAdapter(::handleTrackClick,::handleTrackOptionsClick)
        layout.albumTracks.adapter = adapter

        // Observe view model album detail
        viewModel.detail.observe(viewLifecycleOwner) {
            layout.album = it.album
            adapter.submitList(it.tracks)
        }

        // Observe view model detail loading state
        viewModel.update.observe(viewLifecycleOwner, ::handleAlbumsLoading)

        // Observe view model detail error state
        viewModel.error.observe(viewLifecycleOwner, this::handleTracksError)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        menu.add(0,R.id.action_favorite,0,R.string.title_action_favorite)
            .setIcon(R.drawable.ic_outline_favorite_border_24)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    private fun handleAlbumsLoading(updateState: ViewUpdateState) {
        when(updateState) {
            is ViewUpdateState.Loading -> layout.progressBar.visibility = View.VISIBLE
            is ViewUpdateState.EndLoading -> layout.progressBar.visibility = View.GONE
        }
    }

    private fun handleTracksError(appError: AppError) {
        when(appError) {
            AppError.DEVICE_STORAGE ->
                Toast.makeText(requireContext(),getString(R.string.error_message_storage), Toast.LENGTH_LONG).show()
            AppError.UNKNOWN_ERROR ->
                Toast.makeText(requireContext(),getString(R.string.error_message_unknown), Toast.LENGTH_LONG).show()
        }
    }

    private fun handleTrackClick(track: UiAlbumTrack) {
        val adapter = layout.albumTracks.adapter as AlbumTracksAdapter

        viewModel.playTracks(
            adapter.currentList.indexOf(track),
            adapter.currentList.map { it.id }
        )
    }

    private fun handleTrackOptionsClick(track: UiAlbumTrack, view: View) {
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