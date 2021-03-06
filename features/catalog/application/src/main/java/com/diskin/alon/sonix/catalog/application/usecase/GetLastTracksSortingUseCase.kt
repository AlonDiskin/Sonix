package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.TracksSortingStore
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.UseCase
import io.reactivex.Single
import javax.inject.Inject

class GetLastTracksSortingUseCase @Inject constructor(
    private val sortingStore: TracksSortingStore
) : UseCase<Unit, Single<AppResult<AudioTracksSorting>>> {

    override fun execute(param: Unit): Single<AppResult<AudioTracksSorting>> {
        return sortingStore.getLast()
    }
}