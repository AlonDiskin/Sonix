package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.UseCase
import io.reactivex.Single
import javax.inject.Inject

/**
 * Delete track from device.
 */
class DeleteDeviceTrackUseCase @Inject constructor(
    private val trackRepository: AudioTrackRepository
) : UseCase<Int, Single<AppResult<Unit>>> {

    override fun execute(param: Int): Single<AppResult<Unit>> {
        return trackRepository.delete(param)
    }
}