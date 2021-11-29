package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.SortingStore
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.application.util.UseCase
import io.reactivex.Single
import javax.inject.Inject

class GetLastTracksSortingUseCase @Inject constructor(
    private val sortingStore: SortingStore
) : UseCase<Unit, Single<AppResult<AudioTracksSorting>>> {

    override fun execute(param: Unit): Single<AppResult<AudioTracksSorting>> {
        return sortingStore.getLast()
    }
}