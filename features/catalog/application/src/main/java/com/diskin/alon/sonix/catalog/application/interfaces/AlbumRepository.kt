package com.diskin.alon.sonix.catalog.application.interfaces

import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.catalog.domain.Album
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Observable

interface AlbumRepository {

    fun getAll(sorting: AlbumSorting): Observable<AppResult<List<Album>>>

    fun get(id: Long): Observable<AppResult<Album>>
}