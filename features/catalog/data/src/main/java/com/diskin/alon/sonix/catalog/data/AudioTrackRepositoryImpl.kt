package com.diskin.alon.sonix.catalog.data

import android.os.Build
import android.provider.MediaStore
import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.domain.AudioTrack
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class AudioTrackRepositoryImpl @Inject constructor(
    private val mediaRepository: DeviceMediaStoreRepository
) : AudioTrackRepository {

    private val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    override fun getAll(sorting: AudioTracksSorting): Observable<AppResult<List<AudioTrack>>> {
        return mediaRepository.query(contentUri) { contentResolver ->
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
            val selection = "${MediaStore.Audio.Media.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("audio/mpeg")
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
                selection,
                selectionArgs,
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
            tracks
        }
    }

    override fun get(id: Int): Observable<AppResult<AudioTrack>> {
        return mediaRepository.query(contentUri) { contentResolver ->
            // Define result cursor columns
            val columnId = MediaStore.Audio.Media._ID
            val columnName = MediaStore.Audio.Media.TITLE
            val columnAlbum = MediaStore.Audio.Media.ALBUM
            val columnArtist = MediaStore.Audio.Media.ARTIST
            val columnFormat = MediaStore.Audio.Media.MIME_TYPE
            val columnSize = MediaStore.Audio.Media.SIZE
            val columnPath = MediaStore.Audio.Media.DATA
            val columnDuration = MediaStore.Audio.Media.DURATION

            // Query for track
            val selection = "${MediaStore.Audio.Media._ID} = ?"
            val selectionArgs = arrayOf(id.toString())
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
                selection,
                selectionArgs,
                null)!!

            // Extract track data
            cursor.moveToFirst()

            val trackId = cursor.getInt(cursor.getColumnIndex(columnId))
            val trackName = cursor.getString(cursor.getColumnIndex(columnName))
            val trackAlbum = cursor.getString(cursor.getColumnIndex(columnAlbum))
            val trackArtist = cursor.getString(cursor.getColumnIndex(columnArtist))
            val trackFormat = cursor.getString(cursor.getColumnIndex(columnFormat))
            val trackSize = cursor.getLong(cursor.getColumnIndex(columnSize))
            val trackPath = cursor.getString(cursor.getColumnIndex(columnPath))
            val trackDuration = cursor.getLong(cursor.getColumnIndex(columnDuration))

            // Free cursor and return track
            cursor.close()
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
        }
    }

    override fun delete(id: Int): Single<AppResult<Unit>> {
        return mediaRepository.delete(contentUri,id)
    }
}