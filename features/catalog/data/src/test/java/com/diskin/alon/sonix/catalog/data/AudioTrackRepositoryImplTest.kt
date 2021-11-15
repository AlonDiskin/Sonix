package com.diskin.alon.sonix.catalog.data

import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.core.AudioTrack
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
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
    fun getAllDeviceSortedTracks_WhenQueried() {
        // Given
        val sorting = AudioTracksSorting.DateAdded(true)
        val storeResult: AppResult.Success<List<AudioTrack>> = mockk()

        every { tracksStore.getAll(sorting) } returns Observable.just(storeResult)

        // When
        val observer = repository.getAll(sorting).test()

        // Then
        observer.assertValue(storeResult)
    }
}