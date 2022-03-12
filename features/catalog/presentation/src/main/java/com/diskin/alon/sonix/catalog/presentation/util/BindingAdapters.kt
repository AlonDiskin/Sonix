package com.diskin.alon.sonix.catalog.presentation.util

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.common.presentation.ImageLoader

@BindingAdapter("loadAlbumArt")
fun loadAlbumArt(imageView: ImageView, uri: Uri?) {
    ImageLoader.loadImage(
        imageView.context,
        uri,
        R.drawable.ic_outline_music_note_24,
        imageView
    )
}