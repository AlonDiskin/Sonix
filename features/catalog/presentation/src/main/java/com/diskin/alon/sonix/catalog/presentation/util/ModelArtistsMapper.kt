package com.diskin.alon.sonix.catalog.presentation.util

import android.net.Uri
import com.diskin.alon.sonix.catalog.application.model.ArtistDto
import com.diskin.alon.sonix.catalog.presentation.model.UiArtist
import javax.inject.Inject

class ModelArtistsMapper @Inject constructor() {

    fun map(albums: List<ArtistDto>): List<UiArtist> = albums.map {
        UiArtist(
            it.id,
            it.name,
            Uri.parse(it.artPath)
        )
    }
}