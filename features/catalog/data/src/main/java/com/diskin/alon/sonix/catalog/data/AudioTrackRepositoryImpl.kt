package com.diskin.alon.sonix.catalog.data

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.core.AudioTrack
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class AudioTrackRepositoryImpl @Inject constructor(
    private val tracksStore: DeviceTracksStore
) : AudioTrackRepository {

    override fun getAll(sorting: AudioTracksSorting): Observable<AppResult<List<AudioTrack>>> {
        return tracksStore.getAll(sorting)
    }

    override fun get(id: Int): Observable<AppResult<AudioTrack>> {
        return tracksStore.get(id)
    }

    override fun delete(id: Int): Single<AppResult<Unit>> {
        return tracksStore.delete(id)
    }
}