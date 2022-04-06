package com.diskin.alon.sonix.catalog.application.util

import com.diskin.alon.sonix.catalog.application.model.ArtistDto
import com.diskin.alon.sonix.catalog.domain.Artist
import javax.inject.Inject

class ArtistsMapper @Inject constructor() {

    fun map(albums: List<Artist>): List<ArtistDto> {
        return albums.map {
            ArtistDto(
                it.id,
                it.name,
                it.artPath
            )
        }
    }
}