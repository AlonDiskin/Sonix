package com.diskin.alon.sonix.catalog.application.interfaces

import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.domain.AudioTrack
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Observable
import io.reactivex.Single

interface AudioTrackRepository {

    fun getAll(sorting: AudioTracksSorting): Observable<AppResult<List<AudioTrack>>>

    fun get(id: Int): Observable<AppResult<AudioTrack>>

    fun delete(id: Int): Single<AppResult<Unit>>

    fun getByAlbumId(id: Int): Observable<AppResult<List<AudioTrack>>>
}