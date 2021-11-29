package com.diskin.alon.sonix.catalog.application

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDetailDto
import com.diskin.alon.sonix.catalog.application.usecase.GetDeviceTrackDetailUseCase
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.application.util.TrackDetailMapper
import com.diskin.alon.sonix.catalog.core.AudioTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [GetDeviceTrackDetailUseCase] unit test class.
 */
class GetDeviceTrackDetailUseCaseTest {

    // Test subject
    private lateinit var useCase: GetDeviceTrackDetailUseCase

    // Collaborators
    private val trackRepository: AudioTrackRepository = mockk()
    private val trackMapper: TrackDetailMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetDeviceTrackDetailUseCase(trackRepository, trackMapper)
    }

    @Test
    fun getDeviceTrackDetail_WhenExecuted() {
        // Given
        val id = 1
        val deviceTrack = mockk<AudioTrack>()
        val mappedTrack = mockk<AudioTrackDetailDto>()

        every { trackRepository.get(id) } returns Observable.just(AppResult.Success(deviceTrack))
        every { trackMapper.map(deviceTrack) } returns mappedTrack

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify { trackRepository.get(id) }
        verify { trackMapper.map(deviceTrack) }
        observer.assertValue(AppResult.Success(mappedTrack))
    }
}