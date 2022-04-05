package com.diskin.alon.sonix.catalog.presentation.util

import com.diskin.alon.sonix.catalog.application.model.AudioTrackDto
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbumTrack
import javax.inject.Inject

class ModelAlbumTracksMapper @Inject constructor() {

    fun map(modelTracks: List<AudioTrackDto>): List<UiAlbumTrack> {
        return modelTracks.map {
            UiAlbumTrack(
                it.id,
                it.name
            )
        }
    }
}