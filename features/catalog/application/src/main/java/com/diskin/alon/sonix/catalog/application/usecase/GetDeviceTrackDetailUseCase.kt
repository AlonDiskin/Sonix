package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDetailDto
import com.diskin.alon.sonix.catalog.application.util.TrackDetailMapper
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.UseCase
import com.diskin.alon.sonix.common.application.mapAppResult
import io.reactivex.Observable
import javax.inject.Inject

class GetDeviceTrackDetailUseCase @Inject constructor(
    private val trackRepository: AudioTrackRepository,
    private val trackMapper: TrackDetailMapper
) : UseCase<Int, Observable<AppResult<AudioTrackDetailDto>>> {

    override fun execute(param: Int): Observable<AppResult<AudioTrackDetailDto>> {
        return trackRepository.get(param)
            .mapAppResult(trackMapper::map)
    }
}