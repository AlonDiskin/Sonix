package com.diskin.alon.sonix.catalog.presentation

import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrackDetail

fun createUiTracks(): List<UiAudioTrack> = listOf(
    UiAudioTrack(
        1,
        "name_1",
        "artist_1"
    ),
    UiAudioTrack(
        2,
        "name_2",
        "artist_2"
    ),
    UiAudioTrack(
        3,
        "name_3",
        "artist_3"
    )
)

fun createUiTrackDetail() = UiAudioTrackDetail(
    "track_name",
    "track_artist",
    "track_album",
    "track_path",
    "track_size",
    "track_duration",
    "track_format"
)