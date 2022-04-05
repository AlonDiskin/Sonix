package com.diskin.alon.sonix.catalog.data

import android.content.ContentResolver
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.domain.AudioTrack
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [AudioTrackRepositoryImpl] unit test class.
 */
@RunWith(AndroidJUnit4::class)
class AudioTrackRepositoryImplTest {

    // Test subject
    private lateinit var repository: AudioTrackRepositoryImpl

    // Collaborators
    private val mediaRepository: DeviceMediaStoreRepository = mockk()

    private val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    @Before
    fun setUp() {
        repository = AudioTrackRepositoryImpl(mediaRepository)
    }

    @Test
    fun getAllSortedTracksFromDevice_WhenQueried() {
        // Given
        val sorting = AudioTracksSorting.DateAdded(true)
        val storeResult: AppResult.Success<List<AudioTrack>> = mockk()

        every { mediaRepository.query(contentUri,any<((contentResolver: ContentResolver) -> (List<AudioTrack>))>()) } returns Observable.just(storeResult)

        // When
        val observer = repository.getAll(sorting).test()

        // Then
        observer.assertValue(storeResult)
    }

    @Test
    fun getTrackDetailFromDevice_WhenQueried() {
        // Given
        val id = 1L
        val storeResult = mockk<AppResult.Success<AudioTrack>>()

        every { mediaRepository.query(contentUri,any<((contentResolver: ContentResolver) -> (AudioTrack))>()) } returns Observable.just(storeResult)

        // When
        val observer = repository.get(id).test()

        // Then
        observer.assertValue(storeResult)
    }

    @Test
    fun deleteTrackFromDevice_WhenCommanded() {
        // Given
        val id = 1L
        val storeResult = mockk<AppResult.Success<Unit>>()

        every { mediaRepository.delete(contentUri, id) } returns Single.just(storeResult)

        // When
        val observer = repository.delete(id).test()

        // Then
        observer.assertValue(storeResult)
    }

    @Test
    fun getAlbumTracksFromDevice_WhenQueried() {
        // Given
        val id = 1L
        val storeResult = mockk<AppResult.Success<List<AudioTrack>>>()

        every { mediaRepository.query(contentUri,any<((contentResolver: ContentResolver) -> (List<AudioTrack>))>()) } returns Observable.just(storeResult)

        // When
        val observer = repository.getByAlbumId(id).test()

        // Then
        observer.assertValue(storeResult)
    }
}