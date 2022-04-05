package com.diskin.alon.sonix.catalog.application.interfaces

import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single

interface PlaylistSender {

    fun send(startIndex: Int, ids: List<Long>): Single<AppResult<Unit>>
}