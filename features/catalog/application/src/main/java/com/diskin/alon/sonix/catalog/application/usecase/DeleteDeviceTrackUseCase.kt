package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.application.util.UseCase
import io.reactivex.Single
import javax.inject.Inject

class DeleteDeviceTrackUseCase @Inject constructor(
    private val trackRepository: AudioTrackRepository
) : UseCase<Int,Single<AppResult<Unit>> > {

    override fun execute(param: Int): Single<AppResult<Unit>> {
        return trackRepository.delete(param)
    }
}