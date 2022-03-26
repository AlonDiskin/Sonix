package com.diskin.alon.sonix.catalog.presentation.util

import android.net.Uri
import com.diskin.alon.sonix.catalog.application.model.AlbumDto
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbum
import javax.inject.Inject

class ModelAlbumsMapper @Inject constructor() {

    fun map(albums: List<AlbumDto>): List<UiAlbum> = albums.map {
        UiAlbum(
            it.id,
            it.name,
            it.artist,
            Uri.parse(it.artPath)
        )
    }
}