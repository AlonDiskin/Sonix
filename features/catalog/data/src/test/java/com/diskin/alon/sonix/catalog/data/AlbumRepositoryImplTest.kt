package com.diskin.alon.sonix.catalog.data

import android.content.ContentResolver
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.catalog.domain.Album
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [AlbumRepositoryImpl] unit test.
 */
@RunWith(AndroidJUnit4::class)
class AlbumRepositoryImplTest {

    // Test subject
    private lateinit var repository: AlbumRepositoryImpl

    // Collaborators
    private val mediaRepository: DeviceMediaStoreRepository = mockk()

    private val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Albums.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
    } else {
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    }

    @Before
    fun setUp() {
        repository = AlbumRepositoryImpl(mediaRepository)
    }

    @Test
    fun getAllSortedAlbumsFromDevice_WhenQueried() {
        // Given
        val sorting = AlbumSorting.Name(true)
        val mediaResult: AppResult.Success<List<Album>> = mockk()

        every { mediaRepository.query(contentUri,any<((contentResolver: ContentResolver) -> (List<Album>))>()) } returns Observable.just(mediaResult)

        // When
        val observer = repository.getAll(sorting).test()

        // Then
        observer.assertValue(mediaResult)
    }
}