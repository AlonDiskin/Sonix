package com.diskin.alon.sonix.player.infrastructure.interfaces

import android.net.Uri
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.player.infrastructure.model.TrackMetadata
import io.reactivex.Single

interface TrackMetadataStore {

    fun get(uri: Uri): Single<AppResult<TrackMetadata>>
}