package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.PlaylistSender
import com.diskin.alon.sonix.catalog.application.model.PlayTracksRequest
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.UseCase
import io.reactivex.Single
import javax.inject.Inject

/**
 * Sends a requested play list info to app player to begin playing.
 */
class PlayTracksUseCase @Inject constructor(
    private val playlistSender: PlaylistSender
) : UseCase<PlayTracksRequest, Single<AppResult<Unit>>> {

    override fun execute(param: PlayTracksRequest): Single<AppResult<Unit>> {
        return playlistSender.send(param.startIndex,param.ids)
    }
}