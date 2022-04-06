package com.diskin.alon.sonix.catalog.application.interfaces

import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single

interface ArtistsSortingStore {

    fun getLast(): Single<AppResult<ArtistSorting>>

    fun save(sorting: ArtistSorting): Single<AppResult<Unit>>
}