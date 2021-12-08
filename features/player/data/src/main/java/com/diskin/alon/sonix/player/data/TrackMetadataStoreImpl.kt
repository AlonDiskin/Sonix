package com.diskin.alon.sonix.player.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.player.infrastructure.model.TrackMetadata
import com.diskin.alon.sonix.player.infrastructure.interfaces.TrackMetadataStore
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TrackMetadataStoreImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : TrackMetadataStore {

    override fun get(uri: Uri): Single<AppResult<TrackMetadata>> {
        return Single.create<AppResult<TrackMetadata>> { emitter ->
            val columnName = MediaStore.Audio.Media.TITLE
            val columnAlbum = MediaStore.Audio.Media.ALBUM
            val columnArtist = MediaStore.Audio.Media.ARTIST
            val columnDuration = MediaStore.Audio.Media.DURATION
            val cursor = contentResolver.query(
                uri,
                arrayOf(
                    columnName,
                    columnAlbum,
                    columnArtist,
                    columnDuration
                ),
                null,
                null,
                null
            )!!

            cursor.moveToFirst()

            val trackName = cursor.getString(cursor.getColumnIndex(columnName))
            val trackAlbum = cursor.getString(cursor.getColumnIndex(columnAlbum))
            val trackArtist = cursor.getString(cursor.getColumnIndex(columnArtist))
            val trackDuration = cursor.getLong(cursor.getColumnIndex(columnDuration))

            cursor.close()
            emitter.onSuccess(
                AppResult.Success(
                    TrackMetadata(
                        trackName,
                        if (trackArtist == "<unknown>") "Unknown" else trackArtist,
                        trackAlbum,
                        trackDuration,
                        uri
                    )
                )
            )
        }
            .subscribeOn(Schedulers.io())
            .onErrorReturn { AppResult.Error(AppError.DEVICE_STORAGE) }
    }
}