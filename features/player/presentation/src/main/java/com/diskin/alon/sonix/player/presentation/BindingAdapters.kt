package com.diskin.alon.sonix.player.presentation

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import java.io.ByteArrayInputStream

@BindingAdapter("loadTrackArt")
fun loadTrackArt(imageView: ImageView, uri: Uri?) {
    uri?.let {
        val mr = MediaMetadataRetriever()

        try {
            mr.setDataSource(imageView.context,uri)
            mr.embeddedPicture?.let { byteArr ->
                val inputStream = ByteArrayInputStream(byteArr)
                imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream))
            } ?: run {
                imageView.setImageResource(R.drawable.ic_round_music_note_48)
            }
        } catch (error: Exception) {
            imageView.setImageResource(R.drawable.ic_round_music_note_48)
        }
    }
}