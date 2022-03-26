package com.diskin.alon.sonix.catalog.application.usecase

import com.diskin.alon.sonix.catalog.application.interfaces.AlbumRepository
import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.model.AlbumDetailResponse
import com.diskin.alon.sonix.catalog.application.util.AlbumsMapper
import com.diskin.alon.sonix.catalog.application.util.TracksMapper
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.UseCase
import com.diskin.alon.sonix.common.application.flatMapAppResult
import com.diskin.alon.sonix.common.application.mapAppResult
import io.reactivex.Observable
import javax.inject.Inject

class GetAlbumDetailUseCase @Inject constructor(
    private val trackRepo: AudioTrackRepository,
    private val albumRepo: AlbumRepository,
    private val tracksMapper: TracksMapper,
    private val albumsMapper: AlbumsMapper
) : UseCase<Int,Observable<AppResult<AlbumDetailResponse>>> {

    override fun execute(param: Int): Observable<AppResult<AlbumDetailResponse>> {
        return albumRepo.get(param).mapAppResult{ albumsMapper.map(listOf(it)).first()}
            .flatMapAppResult { albumDto ->
                trackRepo.getByAlbumId(param).mapAppResult(tracksMapper::map)
                    .mapAppResult{ tracksDto ->
                        AlbumDetailResponse(
                            albumDto,
                            tracksDto
                        )
                    }
            }
    }
}