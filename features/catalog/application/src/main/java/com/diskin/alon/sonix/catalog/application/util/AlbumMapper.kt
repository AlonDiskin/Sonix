package com.diskin.alon.sonix.catalog.application.util

import com.diskin.alon.sonix.catalog.application.model.AlbumDto
import com.diskin.alon.sonix.catalog.domain.Album
import javax.inject.Inject

class AlbumMapper @Inject constructor() {

    fun map(albums: List<Album>): List<AlbumDto> {
        return albums.map {
            AlbumDto(
                it.id,
                it.name,
                it.artist,
                it.tracks,
                it.artPath
            )
        }
    }
}