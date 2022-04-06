package com.diskin.alon.sonix.catalog.data

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.diskin.alon.sonix.catalog.application.interfaces.ArtistRepository
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.catalog.domain.Artist
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Observable
import javax.inject.Inject

class ArtistRepositoryImpl @Inject constructor(
    private val mediaRepository: DeviceMediaStoreRepository
) : ArtistRepository {

    private val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Artists.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
    } else {
        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    }

    override fun getAll(sorting: ArtistSorting): Observable<AppResult<List<Artist>>> {
        return mediaRepository.query(contentUri) { contentResolver ->
            val artists = mutableListOf<Artist>()
            val columnId = MediaStore.Audio.Artists._ID
            val columnName = MediaStore.Audio.Artists.ARTIST
            val sortOrder = when(sorting) {
                is ArtistSorting.Name -> if (sorting.ascending)  {
                    "${MediaStore.Audio.Artists.ARTIST} ASC"
                } else {
                    "${MediaStore.Audio.Artists.ARTIST} DESC"
                }

                is ArtistSorting.Date -> if (sorting.ascending)  {
                    "${MediaStore.Audio.Artists.DEFAULT_SORT_ORDER} ASC"
                } else {
                    "${MediaStore.Audio.Artists.DEFAULT_SORT_ORDER} DESC"
                }
            }
            val cursor = contentResolver.query(
                contentUri,
                arrayOf(columnId,columnName),
                null,
                null,
                sortOrder
            )!!

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndex(columnId))
                val name = cursor.getString(cursor.getColumnIndex(columnName))
                val art = getAlbumArtByArtistName(name,contentResolver)

                artists.add(
                    Artist(
                        id,
                        name,
                        art.toString()
                    )
                )
            }

            cursor.close()
            artists
        }
    }

    private fun getAlbumArtByArtistName(artist: String,contentResolver: ContentResolver): Uri {
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Albums.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        }
        val columnId = MediaStore.Audio.Albums.ALBUM_ID
        val selection = "${MediaStore.Audio.Albums.ARTIST} = ?"
        val selectionArgs = arrayOf(artist)
        val cursor = contentResolver.query(
            contentUri,
            arrayOf(columnId),
            selection,
            selectionArgs,
            null
        )!!

        cursor.moveToFirst()

        val albumId = cursor.getLong(cursor.getColumnIndex(columnId))
        val artUri = Uri.parse("content://media/external/audio/albumart/".plus(albumId.toString()))

        cursor.close()
        return artUri
    }
}