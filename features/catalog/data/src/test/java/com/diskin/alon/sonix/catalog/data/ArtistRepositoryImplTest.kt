package com.diskin.alon.sonix.catalog.data

import android.content.ContentResolver
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.catalog.domain.Artist
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [ArtistRepositoryImpl] unit test.
 */
@RunWith(AndroidJUnit4::class)
class ArtistRepositoryImplTest {

    // Test subject
    private lateinit var repository: ArtistRepositoryImpl

    // Collaborators
    private val mediaRepository: DeviceMediaStoreRepository = mockk()

    private val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Artists.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
    } else {
        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    }

    @Before
    fun setUp() {
        repository = ArtistRepositoryImpl(mediaRepository)
    }

    @Test
    fun getAllSortedArtistsFromDevice_WhenQueried() {
        // Given
        val sorting = ArtistSorting.Name(true)
        val mediaResult: AppResult.Success<List<Artist>> = mockk()

        every { mediaRepository.query(contentUri,any<((contentResolver: ContentResolver) -> (List<Artist>))>()) } returns Observable.just(mediaResult)

        // When
        val observer = repository.getAll(sorting).test()

        // Then
        observer.assertValue(mediaResult)
    }
}