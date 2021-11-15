package com.diskin.alon.sonix.catalog.data

import android.content.ContentResolver
import android.provider.MediaStore
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.core.AudioTrack
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTracksStore @Inject constructor(
    contentResolver: ContentResolver,
    errorHandler: ContentResolverErrorHandler
) : DeviceMediaStore(contentResolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, errorHandler) {

    fun getAll(sorting: AudioTracksSorting): Observable<AppResult<List<AudioTrack>>> {
        return query{ tracksQuery(sorting) }
    }

    private fun tracksQuery(sorting: AudioTracksSorting): List<AudioTrack> {
        val tracks: MutableList<AudioTrack> = arrayListOf()

        // Define result cursor columns
        val columnId = MediaStore.Audio.Media._ID
        val columnName = MediaStore.Audio.Media.TITLE
        val columnAlbum = MediaStore.Audio.Media.ALBUM
        val columnArtist = MediaStore.Audio.Media.ARTIST
        val columnFormat = MediaStore.Audio.Media.MIME_TYPE
        val columnSize = MediaStore.Audio.Media.SIZE
        val columnPath = MediaStore.Audio.Media.DATA
        val columnDuration = MediaStore.Audio.Media.DURATION

        // Query provider
        val sortOrder = when(sorting) {
            is AudioTracksSorting.DateAdded -> if (sorting.ascending)  {
                "${MediaStore.Audio.Media.DATE_MODIFIED} ASC"
            } else {
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            }

            is AudioTracksSorting.ArtistName -> if (sorting.ascending)  {
                "${MediaStore.Audio.Media.ARTIST} ASC"
            } else {
                "${MediaStore.Audio.Media.ARTIST} DESC"
            }
        }
        val cursor = contentResolver.query(
            contentUri,
            arrayOf(
                columnId,
                columnName,
                columnAlbum,
                columnArtist,
                columnFormat,
                columnSize,
                columnPath,
                columnDuration
            ),
            null,
            null,
            sortOrder)!!

        while (cursor.moveToNext()) {
            val trackId = cursor.getInt(cursor.getColumnIndex(columnId))
            val trackName = cursor.getString(cursor.getColumnIndex(columnName))
            val trackAlbum = cursor.getString(cursor.getColumnIndex(columnAlbum))
            val trackArtist = cursor.getString(cursor.getColumnIndex(columnArtist))
            val trackFormat = cursor.getString(cursor.getColumnIndex(columnFormat))
            val trackSize = cursor.getLong(cursor.getColumnIndex(columnSize))
            val trackPath = cursor.getString(cursor.getColumnIndex(columnPath))
            val trackDuration = cursor.getLong(cursor.getColumnIndex(columnDuration))

            tracks.add(
                AudioTrack(
                    trackId,
                    trackPath,
                    trackName,
                    trackArtist,
                    trackAlbum,
                    trackSize,
                    trackDuration,
                    trackFormat
                )
            )
        }

        // Free cursor and return tracks
        cursor.close()
        return tracks
    }
}