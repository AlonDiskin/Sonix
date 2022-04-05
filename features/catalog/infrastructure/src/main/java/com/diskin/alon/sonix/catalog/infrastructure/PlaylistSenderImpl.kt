package com.diskin.alon.sonix.catalog.infrastructure

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.diskin.alon.sonix.catalog.application.interfaces.PlaylistSender
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.catalog.events.SelectedPlaylistPublisher
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PlaylistSenderImpl @Inject constructor(
    private val playlistPublisher: SelectedPlaylistPublisher
) : PlaylistSender {

    override fun send(startIndex: Int, ids: List<Long>): Single<AppResult<Unit>> {
        return Single.create<AppResult<Unit>> { emitter ->
            val audioContentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            playlistPublisher.publish(
                SelectedPlaylist(
                    startIndex,
                    ids.map { id ->
                        Uri.parse(
                            audioContentUri.toString()
                                .plus("/$id")
                        )
                    }
                )
            )
            emitter.onSuccess(AppResult.Success(Unit))

        }.subscribeOn(Schedulers.computation())
    }
}