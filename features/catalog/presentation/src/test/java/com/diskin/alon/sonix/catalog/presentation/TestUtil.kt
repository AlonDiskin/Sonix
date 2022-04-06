package com.diskin.alon.sonix.catalog.presentation

import android.net.Uri
import com.diskin.alon.sonix.catalog.presentation.model.*

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

fun createUiArtists(): List<UiArtist> = listOf(
    UiArtist(1,"album_1_name",Uri.EMPTY),
    UiArtist(2,"album_2_name", Uri.EMPTY),
    UiArtist(3,"album_3_name",Uri.EMPTY)
)

fun createUiAlbums(): List<UiAlbum> = listOf(
    UiAlbum(1,"album_1_name","album_1_artist", Uri.EMPTY),
    UiAlbum(2,"album_2_name","album_2_artist", Uri.EMPTY),
    UiAlbum(3,"album_3_name","album_3_artist", Uri.EMPTY)
)

fun createUiAlbumDetail(): UiAlbumDetail = UiAlbumDetail(
    createUiAlbums().first(),
    listOf(
        UiAlbumTrack(1,"name_1"),
        UiAlbumTrack(2,"name_2"),
        UiAlbumTrack(3,"name_3")
    )
)