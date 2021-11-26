package com.diskin.alon.sonix.catalog.application.util

import com.diskin.alon.sonix.catalog.application.model.AudioTrackDetailDto
import com.diskin.alon.sonix.catalog.core.AudioTrack
import javax.inject.Inject

class TrackDetailMapper @Inject constructor() {

    fun map(track: AudioTrack): AudioTrackDetailDto {
        return AudioTrackDetailDto(
            track.name,
            track.artist,
            track.album,
            track.path,
            track.size,
            track.duration,
            track.format
        )
    }
}