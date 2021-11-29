package com.diskin.alon.sonix.catalog.presentation.util

import com.diskin.alon.sonix.catalog.application.model.AudioTrackDto
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack
import javax.inject.Inject

/**
 * Maps [AudioTrackDto]s to [UiAudioTrack]s.
 */
class ModelTracksMapper @Inject constructor() {

    fun map(modelTracks: List<AudioTrackDto>): List<UiAudioTrack> {
        return modelTracks.map { 
            UiAudioTrack(
                it.id,
                it.name,
                it.artist
            )
        }
    }
}
