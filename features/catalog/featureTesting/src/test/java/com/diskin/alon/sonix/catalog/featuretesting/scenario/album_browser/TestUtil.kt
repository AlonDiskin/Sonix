package com.diskin.alon.sonix.catalog.featuretesting.scenario.album_browser

import android.database.MatrixCursor
import android.os.Looper
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.presentation.R
import org.robolectric.Shadows

data class DeviceAlbum(val id: Long,val name: String,val artist: String,val tracks: Int)

fun openAlbumsSortingMenu() {
    openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    onView(withText(R.string.title_sort_albums_menu))
        .perform(click())
    Shadows.shadowOf(Looper.getMainLooper()).idle()
}

fun createDeviceAlbums() = listOf(
    DeviceAlbum(1,"album_1","artist_1",2),
    DeviceAlbum(2,"album_2","artist_2",1),
    DeviceAlbum(3,"album_3","artist_3",4),
    DeviceAlbum(4,"album_4","artist_4",3)
)

fun createAlbumMediaStoreCursor(albums: List<DeviceAlbum>): MatrixCursor {
    val cursor = MatrixCursor(
        arrayOf(
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.ARTIST
        ),
        albums.size
    )

    albums.forEach{ cursor.addRow(arrayOf(it.id,it.name,it.tracks,it.artist))}
    return cursor
}

fun createAudioMediaStoreCursor(tracks: List<DeviceTrack>): MatrixCursor {
    val cursor = MatrixCursor(
        arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        ),
        tracks.size
    )

    tracks.forEach{
        cursor.addRow(arrayOf(it.id,it.name,it.album,it.artist,it.format,it.size,it.path,it.duration))
    }
    return cursor
}

fun createDeviceAlbumTracks(): List<DeviceTrack> = listOf(
    DeviceTrack(
        1,
        "path_1",
        "title_1",
        "artist_1",
        "album_1",
        20000L,
        40000L,
        "audio/mp3"
    ),
    DeviceTrack(
        1,
        "path_2",
        "title_2",
        "artist_1",
        "album_1",
        10000L,
        50000L,
        "audio/mp3"
    ),
    DeviceTrack(
        1,
        "path_3",
        "title_3",
        "artist_1",
        "album_1",
        30000L,
        20000L,
        "audio/mp3"
    ),
    DeviceTrack(
        1,
        "path_3",
        "title_4",
        "artist_1",
        "album_1",
        50000L,
        30000L,
        "audio/mp3"
    )
)

data class DeviceTrack(val id: Long,
                       val path: String,
                       val name: String,
                       val artist: String,
                       val album: String,
                       val size: Long,
                       val duration: Long,
                       val format: String)