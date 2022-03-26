package com.diskin.alon.sonix.catalog.application

import com.diskin.alon.sonix.catalog.application.interfaces.AlbumRepository
import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.model.AlbumDetailResponse
import com.diskin.alon.sonix.catalog.application.model.AlbumDto
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDto
import com.diskin.alon.sonix.catalog.application.usecase.GetAlbumDetailUseCase
import com.diskin.alon.sonix.catalog.application.util.AlbumsMapper
import com.diskin.alon.sonix.catalog.application.util.TracksMapper
import com.diskin.alon.sonix.catalog.domain.Album
import com.diskin.alon.sonix.catalog.domain.AudioTrack
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class GetAlbumDetailUseCaseTest {

    // Test subject
    private lateinit var useCase: GetAlbumDetailUseCase

    // Collaborators
    private val trackRepo: AudioTrackRepository = mockk()
    private val albumRepo: AlbumRepository = mockk()
    private val tracksMapper: TracksMapper = mockk()
    private val albumsMapper: AlbumsMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetAlbumDetailUseCase(trackRepo, albumRepo, tracksMapper, albumsMapper)
    }

    @Test
    fun getAlbumTracks_WhenExecuted() {
        // Given
        val albumId = 1
        val repoTracks = mockk<List<AudioTrack>>()
        val repoAlbum = mockk<Album>()
        val mappedTracks = mockk<List<AudioTrackDto>>()
        val mappedAlbum = mockk<AlbumDto>()

        every { trackRepo.getByAlbumId(albumId) } returns Observable.just(AppResult.Success(repoTracks))
        every { albumRepo.get(albumId) } returns Observable.just(AppResult.Success(repoAlbum))
        every { tracksMapper.map(repoTracks) } returns mappedTracks
        every { albumsMapper.map(listOf(repoAlbum)) } returns listOf(mappedAlbum)

        // When
        val observer = useCase.execute(albumId).test()

        // Then
        observer.assertValue(AppResult.Success(AlbumDetailResponse(mappedAlbum,mappedTracks)))
    }
}