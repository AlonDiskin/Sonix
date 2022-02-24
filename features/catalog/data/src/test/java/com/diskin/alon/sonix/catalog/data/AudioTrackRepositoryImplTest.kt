package com.diskin.alon.sonix.catalog.data

import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.core.AudioTrack
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * [AudioTrackRepositoryImpl] unit test class.
 */
class AudioTrackRepositoryImplTest {

    // Test subject
    private lateinit var repository: AudioTrackRepositoryImpl

    // Collaborators
    private val tracksStore: DeviceTracksStore = mockk()

    @Before
    fun setUp() {
        repository = AudioTrackRepositoryImpl(tracksStore)
    }

    @Test
    fun getAllSortedTracksFromDevice_WhenQueried() {
        // Given
        val sorting = AudioTracksSorting.DateAdded(true)
        val storeResult: AppResult.Success<List<AudioTrack>> = mockk()

        every { tracksStore.getAll(sorting) } returns Observable.just(storeResult)

        // When
        val observer = repository.getAll(sorting).test()

        // Then
        observer.assertValue(storeResult)
    }

    @Test
    fun getTrackDetailFromDevice_WhenQueried() {
        // Given
        val id = 1
        val storeResult = mockk<AppResult.Success<AudioTrack>>()

        every { tracksStore.get(id) } returns Observable.just(storeResult)

        // When
        val observer = repository.get(id).test()

        // Then
        observer.assertValue(storeResult)
    }

    @Test
    fun deleteTrackFromDevice_WhenCommanded() {
        // Given
        val id = 1
        val storeResult = mockk<AppResult.Success<Unit>>()

        every { tracksStore.delete(id) } returns Single.just(storeResult)

        // When
        val observer = repository.delete(id).test()

        // Then
        observer.assertValue(storeResult)
    }
}