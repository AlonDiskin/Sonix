package com.diskin.alon.sonix.common.presentation

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide

object ImageLoader {

    fun loadImage(context: Context,uri: Uri?,@DrawableRes placeHolder: Int,imageView: ImageView) {
        Glide
            .with(context)
            .load(uri)
            .centerCrop()
            .placeholder(placeHolder)
            .into(imageView)
    }
}