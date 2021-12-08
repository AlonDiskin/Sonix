package com.diskin.alon.sonix.player.infrastructure

import android.net.Uri
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.player.infrastructure.model.AudioPlayerTrack
import com.diskin.alon.sonix.player.infrastructure.model.TrackMetadata

fun createPlaylist(): SelectedPlaylist {
    return SelectedPlaylist(
        1,
        emptyList()
    )
}

fun createAudioPlayerTrack(): AudioPlayerTrack {
    return AudioPlayerTrack(Uri.EMPTY,true)
}

fun createMetadata(): TrackMetadata {
    return TrackMetadata(
        "name",
        "artist",
        "album",
        123456L,
        Uri.EMPTY
    )
}