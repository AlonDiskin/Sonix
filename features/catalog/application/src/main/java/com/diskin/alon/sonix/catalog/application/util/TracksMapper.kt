package com.diskin.alon.sonix.catalog.application.util

import com.diskin.alon.sonix.catalog.domain.AudioTrack
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDto
import javax.inject.Inject

class TracksMapper @Inject constructor() {
    fun map(tracks: List<AudioTrack>): List<AudioTrackDto> {
        return tracks.map {
            AudioTrackDto(
                it.id,
                it.name,
                it.artist
            )
        }
    }
}