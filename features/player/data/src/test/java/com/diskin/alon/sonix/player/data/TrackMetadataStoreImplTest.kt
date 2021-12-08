package com.diskin.alon.sonix.player.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.player.infrastructure.model.TrackMetadata
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackMetadataStoreImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var store: TrackMetadataStoreImpl

    // Collaborators
    private val contentResolver: ContentResolver = mockk()

    @Before
    fun setUp() {
        store = TrackMetadataStoreImpl(contentResolver)
    }

    @Test
    fun getTrackMetadata_WhenQueried() {
        // Given
        val uri = mockk<Uri>()
        val cursorData= createMetadataCursor()
        val metadata = TrackMetadata(
            cursorData.second[0] as String,
            cursorData.second[1] as String,
            cursorData.second[2] as String,
            cursorData.second[3] as Long,
            uri
        )
        val columns = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )

        every { contentResolver.query(uri,columns,null,null,null) } returns cursorData.first

        // When
        val observer = store.get(uri).test()

        // Then
        assertThat(cursorData.first.isClosed).isTrue()
        observer.assertValue(AppResult.Success(metadata))
    }

    @Test
    fun handleError_WhenMetadataFetchingFail() {
        // Given

        every { contentResolver.query(any(),any(),any(),any(),any()) } returns null

        // When
        val observer = store.get(mockk()).test()

        // Then
        observer.assertValue(AppResult.Error(AppError.DEVICE_STORAGE))
    }
}