package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.AlbumRepository
import com.diskin.alon.sonix.catalog.application.interfaces.AlbumsSortingStore
import com.diskin.alon.sonix.catalog.application.model.AlbumsRequest
import com.diskin.alon.sonix.catalog.application.model.AlbumsResponse
import com.diskin.alon.sonix.catalog.application.util.AlbumMapper
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.UseCase
import com.diskin.alon.sonix.common.application.flatMapAppResult
import com.diskin.alon.sonix.common.application.mapAppResult
import io.reactivex.Observable
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val sortingStore: AlbumsSortingStore,
    private val repository: AlbumRepository,
    private val mapper: AlbumMapper
) : UseCase<AlbumsRequest,Observable<AppResult<AlbumsResponse>>> {

    override fun execute(param: AlbumsRequest): Observable<AppResult<AlbumsResponse>> {
        return when(param) {
            is AlbumsRequest.LastSorted ->
                sortingStore.getLast()
                .toObservable()
                .flatMapAppResult { sorting ->
                    repository.getAll(sorting)
                        .mapAppResult { albums ->
                            AlbumsResponse(mapper.map(albums),sorting)
                        }
                }

            is AlbumsRequest.GetSorted ->
                sortingStore.save(param.sorting)
                .flatMapObservable { repository.getAll(param.sorting) }
                .mapAppResult { AlbumsResponse(mapper.map(it),param.sorting)}
        }
    }
}