package com.diskin.alon.sonix.catalog.presentation.controller

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.databinding.DialogAudioTrackDetailBinding
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AudioTrackDetailViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class AudioTrackDetailDialog : DialogFragment(){

    private val viewModel: AudioTrackDetailViewModel by viewModels()
    private lateinit var layout: DialogAudioTrackDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return layout.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            layout = DialogAudioTrackDetailBinding.inflate(requireActivity().layoutInflater)

            builder.setPositiveButton("close") { _, _ -> dialog?.dismiss()}
            builder.setTitle(getString(R.string.title_dialog_track_detail))
            builder.setView(layout.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null for dialog creation")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.trackDetail.observe(viewLifecycleOwner) { trackDetail ->
            layout.trackDetail = trackDetail
        }
        viewModel.error.observe(viewLifecycleOwner) { appError ->
            when(appError) {
                com.diskin.alon.sonix.common.application.AppError.DEVICE_STORAGE ->
                    Toast.makeText(requireContext(),getString(R.string.error_message_storage), Toast.LENGTH_LONG).show()
                com.diskin.alon.sonix.common.application.AppError.UNKNOWN_ERROR ->
                    Toast.makeText(requireContext(),getString(R.string.error_message_unknown), Toast.LENGTH_LONG).show()
            }
        }
    }
}