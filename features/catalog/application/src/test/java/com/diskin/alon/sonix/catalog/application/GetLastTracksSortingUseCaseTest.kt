package com.diskin.alon.sonix.catalog.application

import com.diskin.alon.sonix.catalog.application.interfaces.SortingStore
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.usecase.GetLastTracksSortingUseCase
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetLastTracksSortingUseCaseTest {

    // Test subject
    private lateinit var useCase: GetLastTracksSortingUseCase

    // Collaborators
    private val sortingStore: SortingStore = mockk()

    @Before
    fun setUp() {
        useCase = GetLastTracksSortingUseCase(sortingStore)
    }

    @Test
    fun getLastSortingType_WhenExecuted() {
        // Given
        val sorting = mockk<AudioTracksSorting>()

        every { sortingStore.getLast() } returns Single.just(AppResult.Success(sorting))

        // When
        val observer = useCase.execute(Unit).test()

        // Then
        verify { sortingStore.getLast() }
        observer.assertValue(AppResult.Success(sorting))
    }
}