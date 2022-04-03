package com.diskin.alon.sonix.catalog.application

import com.diskin.alon.sonix.catalog.application.interfaces.AudioTrackRepository
import com.diskin.alon.sonix.catalog.application.usecase.DeleteDeviceTrackUseCase
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class DeleteDeviceTrackUseCaseTest {

    // Test subject
    private lateinit var useCase: DeleteDeviceTrackUseCase

    // Collaborators
    private val trackRepository: AudioTrackRepository = mockk()

    @Before
    fun setUp() {
        useCase = DeleteDeviceTrackUseCase(trackRepository)
    }

    @Test
    fun deleteTrackFromDevice_WhenExecuted() {
        // Given
        val id = 1L
        val repoDeleteResult = mockk<AppResult.Success<Unit>>()

        every { trackRepository.delete(id) } returns Single.just(repoDeleteResult)

        // When
        val observer = useCase.execute(id).test()

        // Then
        verify { trackRepository.delete(id) }
        observer.assertValue(repoDeleteResult)
    }
}