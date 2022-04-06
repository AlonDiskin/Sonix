package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.ArtistRepository
import com.diskin.alon.sonix.catalog.application.interfaces.ArtistsSortingStore
import com.diskin.alon.sonix.catalog.application.model.ArtistsRequest
import com.diskin.alon.sonix.catalog.application.model.ArtistsResponse
import com.diskin.alon.sonix.catalog.application.util.ArtistsMapper
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.UseCase
import com.diskin.alon.sonix.common.application.flatMapAppResult
import com.diskin.alon.sonix.common.application.mapAppResult
import io.reactivex.Observable
import javax.inject.Inject

class GetArtistsUseCase @Inject constructor(
    private val sortingStore: ArtistsSortingStore,
    private val repository: ArtistRepository,
    private val mapper: ArtistsMapper
) : UseCase<ArtistsRequest,Observable<AppResult<ArtistsResponse>>> {

    override fun execute(param: ArtistsRequest): Observable<AppResult<ArtistsResponse>> {
        return when(param) {
            is ArtistsRequest.LastSorted ->
                sortingStore.getLast()
                .toObservable()
                .flatMapAppResult { sorting ->
                    repository.getAll(sorting)
                        .mapAppResult { albums ->
                            ArtistsResponse(mapper.map(albums),sorting)
                        }
                }

            is ArtistsRequest.GetSorted ->
                sortingStore.save(param.sorting)
                .flatMapObservable { repository.getAll(param.sorting) }
                .mapAppResult { ArtistsResponse(mapper.map(it),param.sorting)}
        }
    }
}