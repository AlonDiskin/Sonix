package com.diskin.alon.sonix.catalog.data

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.diskin.alon.sonix.catalog.application.interfaces.AlbumRepository
import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.catalog.domain.Album
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Observable
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val mediaRepository: DeviceMediaStoreRepository
) : AlbumRepository {

    private val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Albums.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
    } else {
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    }

    override fun getAll(sorting: AlbumSorting): Observable<AppResult<List<Album>>> {
        return mediaRepository.query(contentUri) { contentResolver ->
            val albums = mutableListOf<Album>()
            val columnId = MediaStore.Audio.Albums.ALBUM_ID
            val columnName = MediaStore.Audio.Albums.ALBUM
            val columnTracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS
            val columnArtist = MediaStore.Audio.Albums.ARTIST
            val sortOrder = when(sorting) {
                is AlbumSorting.Artist -> if (sorting.ascending)  {
                    "${MediaStore.Audio.Albums.ARTIST} ASC"
                } else {
                    "${MediaStore.Audio.Albums.ARTIST} DESC"
                }

                is AlbumSorting.Name -> if (sorting.ascending)  {
                    "${MediaStore.Audio.Albums.ALBUM} ASC"
                } else {
                    "${MediaStore.Audio.Albums.ALBUM} DESC"
                }
            }
            val cursor = contentResolver.query(
                contentUri,
                arrayOf(columnId,columnName,columnArtist,columnTracks),
                null,
                null,
                sortOrder
            )!!

            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndex(columnId))
                val albumName = cursor.getString(cursor.getColumnIndex(columnName))
                val artist = cursor.getString(cursor.getColumnIndex(columnArtist))
                val tracksCount = cursor.getInt(cursor.getColumnIndex(columnTracks))
                val artUri = Uri.parse("content://media/external/audio/albumart/".plus(id.toString()))

                albums.add(
                    Album(
                        id,
                        albumName,
                        artist,
                        tracksCount,
                        artUri.toString()
                    )
                )
            }

            cursor.close()
            albums
        }
    }

    override fun get(id: Int): Observable<AppResult<Album>> {
        return mediaRepository.query(contentUri) { contentResolver ->
            val columnId = MediaStore.Audio.Albums.ALBUM_ID
            val columnName = MediaStore.Audio.Albums.ALBUM
            val columnTracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS
            val columnArtist = MediaStore.Audio.Albums.ARTIST
            val selection = "${MediaStore.Audio.Albums.ALBUM_ID} = ?"
            val selectionArgs = arrayOf(id.toString())
            val cursor = contentResolver.query(
                contentUri,
                arrayOf(columnId,columnName,columnArtist,columnTracks),
                selection,
                selectionArgs,
                null
            )!!

            cursor.moveToFirst()

            val albumName = cursor.getString(cursor.getColumnIndex(columnName))
            val artist = cursor.getString(cursor.getColumnIndex(columnArtist))
            val tracksCount = cursor.getInt(cursor.getColumnIndex(columnTracks))
            val artUri = Uri.parse("content://media/external/audio/albumart/".plus(id.toString()))

            cursor.close()
            Album(
                id,
                albumName,
                artist,
                tracksCount,
                artUri.toString()
            )
        }
    }
}