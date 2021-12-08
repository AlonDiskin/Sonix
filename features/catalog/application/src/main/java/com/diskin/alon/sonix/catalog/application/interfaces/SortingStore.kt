package com.diskin.alon.sonix.catalog.application.interfaces

import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single

interface SortingStore {

    fun getLast(): Single<AppResult<AudioTracksSorting>>

    fun save(sorting: AudioTracksSorting): Single<AppResult<Unit>>
}