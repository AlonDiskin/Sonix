package com.diskin.alon.sonix.catalog.application

import com.diskin.alon.sonix.catalog.application.interfaces.AlbumRepository
import com.diskin.alon.sonix.catalog.application.interfaces.AlbumsSortingStore
import com.diskin.alon.sonix.catalog.application.model.AlbumDto
import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.catalog.application.model.AlbumsRequest
import com.diskin.alon.sonix.catalog.application.model.AlbumsResponse
import com.diskin.alon.sonix.catalog.application.usecase.GetAlbumsUseCase
import com.diskin.alon.sonix.catalog.application.util.AlbumsMapper
import com.diskin.alon.sonix.catalog.domain.Album
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetAlbumsUseCaseTest {

    // Test subject
    private lateinit var useCase: GetAlbumsUseCase

    // Collaborators
    private val sortingStore: AlbumsSortingStore = mockk()
    private val repository: AlbumRepository = mockk()
    private val mapper: AlbumsMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetAlbumsUseCase(sortingStore, repository,mapper)
    }

    @Test
    fun getAllAlbumsByLastSorting_WhenExecutedForLastSortedRequest() {
        // Given
        val sorting = AlbumSorting.Artist(true)
        val albums = listOf<Album>()
        val mappedAlbums = listOf<AlbumDto>()

        every { sortingStore.getLast() } returns Single.just(AppResult.Success(sorting))
        every { repository.getAll(sorting) } returns Observable.just(AppResult.Success(albums))
        every { mapper.map(albums) } returns mappedAlbums

        // When
        val observer = useCase.execute(AlbumsRequest.LastSorted).test()

        // Then
        verify { sortingStore.getLast() }
        verify { repository.getAll(sorting) }
        verify { mapper.map(albums) }
        observer.assertValue(AppResult.Success(AlbumsResponse(mappedAlbums,sorting)))
    }

    @Test
    fun getAllAlbumsBySorting_WhenExecutedForSortedRequest() {
        // Given
        val request = AlbumsRequest.GetSorted(AlbumSorting.Name(false))
        val albums = listOf<Album>()
        val mappedAlbums = listOf<AlbumDto>()

        every { repository.getAll(request.sorting) } returns Observable.just(AppResult.Success(albums))
        every { sortingStore.save(request.sorting) } returns Single.just(AppResult.Success(Unit))
        every { mapper.map(albums) } returns mappedAlbums

        // When
        val observer = useCase.execute(request).test()

        // Then
        verify { sortingStore.save(request.sorting) }
        verify { repository.getAll(request.sorting) }
        verify { mapper.map(albums) }
        observer.assertValue(AppResult.Success(AlbumsResponse(mappedAlbums,request.sorting)))
    }
}