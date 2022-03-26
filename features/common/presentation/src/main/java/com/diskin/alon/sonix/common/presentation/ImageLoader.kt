package com.diskin.alon.sonix.common.presentation

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

object ImageLoader {

    fun loadImage(context: Context,uri: Uri?,@DrawableRes default: Int,imageView: ImageView) {
        Glide.with(context)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(default)
            .into(imageView)
    }
}