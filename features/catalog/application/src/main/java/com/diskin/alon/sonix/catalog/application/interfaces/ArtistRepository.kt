package com.diskin.alon.sonix.catalog.application.interfaces

import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.catalog.domain.Artist
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Observable

interface ArtistRepository {

    fun getAll(sorting: ArtistSorting): Observable<AppResult<List<Artist>>>
}