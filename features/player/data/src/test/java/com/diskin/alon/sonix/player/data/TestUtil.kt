package com.diskin.alon.sonix.player.data

import android.database.MatrixCursor
import android.provider.MediaStore

fun createMetadataCursor(): Pair<MatrixCursor,Array<Any>> {
    val values: Array<Any> = arrayOf(
        "title",
        "artist",
        "album",
        12000L
    )
    val cursor = MatrixCursor(
        arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        ),
        1
    )

    cursor.addRow(values)
    return Pair(cursor,values)
}