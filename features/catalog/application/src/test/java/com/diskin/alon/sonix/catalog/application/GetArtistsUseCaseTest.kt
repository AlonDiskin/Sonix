package com.diskin.alon.sonix.catalog.application

import com.diskin.alon.sonix.catalog.application.interfaces.ArtistRepository
import com.diskin.alon.sonix.catalog.application.interfaces.ArtistsSortingStore
import com.diskin.alon.sonix.catalog.application.model.ArtistDto
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.catalog.application.model.ArtistsRequest
import com.diskin.alon.sonix.catalog.application.model.ArtistsResponse
import com.diskin.alon.sonix.catalog.application.usecase.GetArtistsUseCase
import com.diskin.alon.sonix.catalog.application.util.ArtistsMapper
import com.diskin.alon.sonix.catalog.domain.Artist
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * [GetArtistsUseCase] unit tests class.
 */
class GetArtistsUseCaseTest {

    // Test subject
    private lateinit var useCase: GetArtistsUseCase

    // Collaborators
    private val sortingStore: ArtistsSortingStore = mockk()
    private val repository: ArtistRepository = mockk()
    private val mapper: ArtistsMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetArtistsUseCase(sortingStore, repository,mapper)
    }

    @Test
    fun getAllArtistsByLastSorting_WhenExecutedForLastSortedRequest() {
        // Given
        val sorting = ArtistSorting.Name(true)
        val artists = listOf<Artist>()
        val mappedArtists = listOf<ArtistDto>()

        every { sortingStore.getLast() } returns Single.just(AppResult.Success(sorting))
        every { repository.getAll(sorting) } returns Observable.just(AppResult.Success(artists))
        every { mapper.map(artists) } returns mappedArtists

        // When
        val observer = useCase.execute(ArtistsRequest.LastSorted).test()

        // Then
        verify { sortingStore.getLast() }
        verify { repository.getAll(sorting) }
        verify { mapper.map(artists) }
        observer.assertValue(AppResult.Success(ArtistsResponse(mappedArtists,sorting)))
    }

    @Test
    fun getAllArtistsBySorting_WhenExecutedForSortedRequest() {
        // Given
        val request = ArtistsRequest.GetSorted(ArtistSorting.Name(false))
        val artists = listOf<Artist>()
        val mappedArtists = listOf<ArtistDto>()

        every { repository.getAll(request.sorting) } returns Observable.just(AppResult.Success(artists))
        every { sortingStore.save(request.sorting) } returns Single.just(AppResult.Success(Unit))
        every { mapper.map(artists) } returns mappedArtists

        // When
        val observer = useCase.execute(request).test()

        // Then
        verify { sortingStore.save(request.sorting) }
        verify { repository.getAll(request.sorting) }
        verify { mapper.map(artists) }
        observer.assertValue(AppResult.Success(ArtistsResponse(mappedArtists,request.sorting)))
    }
}