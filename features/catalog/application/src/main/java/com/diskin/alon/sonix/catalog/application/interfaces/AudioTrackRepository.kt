package com.diskin.alon.sonix.catalog.application.interfaces

import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.core.AudioTrack
import io.reactivex.Observable

interface AudioTrackRepository {

    fun getAll(sorting: AudioTracksSorting): Observable<AppResult<List<AudioTrack>>>
}