package com.diskin.alon.sonix.catalog.application

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.interfaces.TracksSortingStore
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDto
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.usecase.GetSortedDeviceTracksUseCase
import com.diskin.alon.sonix.catalog.application.util.TracksMapper
import com.diskin.alon.sonix.catalog.domain.AudioTrack
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetSortedDeviceTracksUseCaseTest {

    // Test subject
    private lateinit var useCase: GetSortedDeviceTracksUseCase

    // Collaborators
    private val tracksRepository: AudioTrackRepository = mockk()
    private val sortingStore: TracksSortingStore = mockk()
    private val tracksMapper: TracksMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetSortedDeviceTracksUseCase(tracksRepository, sortingStore, tracksMapper)
    }

    @Test
    fun saveSortingAndGetTracks_WhenExecuted() {
        // Given
        val sorting = mockk<AudioTracksSorting>()
        val reposTracks = mockk<List<AudioTrack>>()
        val mappedTracks = mockk<List<AudioTrackDto>>()

        every { sortingStore.save(sorting) } returns Single.just(AppResult.Success(Unit))
        every { tracksRepository.getAll(sorting) } returns Observable.just(AppResult.Success(reposTracks))
        every { tracksMapper.map(reposTracks) } returns mappedTracks

        // When
        val observer = useCase.execute(sorting).test()

        // Then
        verify { sortingStore.save(sorting) }
        verify { tracksRepository.getAll(sorting) }
        verify { tracksMapper.map(reposTracks) }
        observer.assertValue(AppResult.Success(mappedTracks))
    }
}