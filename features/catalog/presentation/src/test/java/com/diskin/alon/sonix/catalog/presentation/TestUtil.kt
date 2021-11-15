package com.diskin.alon.sonix.catalog.presentation

import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack

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