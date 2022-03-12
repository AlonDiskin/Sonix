package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.interfaces.TracksSortingStore
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDto
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.util.*
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.UseCase
import com.diskin.alon.sonix.common.application.mapAppResult
import io.reactivex.Observable
import javax.inject.Inject

class GetSortedDeviceTracksUseCase @Inject constructor(
    private val tracksRepository: AudioTrackRepository,
    private val sortingStore: TracksSortingStore,
    private val tracksMapper: TracksMapper
) : UseCase<AudioTracksSorting, Observable<AppResult<List<AudioTrackDto>>>> {

    override fun execute(param: AudioTracksSorting): Observable<AppResult<List<AudioTrackDto>>> {
        return sortingStore.save(param)
            .flatMapObservable {
                tracksRepository.getAll(param)
                    .mapAppResult(tracksMapper::map)
            }
    }
}