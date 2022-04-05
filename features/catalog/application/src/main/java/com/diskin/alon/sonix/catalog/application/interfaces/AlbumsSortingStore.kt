package com.diskin.alon.sonix.catalog.application.interfaces

import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single

interface AlbumsSortingStore {

    fun getLast(): Single<AppResult<AlbumSorting>>

    fun save(sorting: AlbumSorting): Single<AppResult<Unit>>
}