package com.diskin.alon.sonix.catalog.data

import android.content.Context
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.util.AppError
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboCursor

/**
 * [DeviceTracksStore] unit test class.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class DeviceTracksStoreTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var tracksStore: DeviceTracksStore

    // Collaborators
    private val errorHandler: ContentResolverErrorHandler = mockk()

    // Stub data
    private val cursor = RoboCursor()

    @Before
    fun setUp() {
        // Stub mock error handler
        every { errorHandler.handle(any()) } returns AppError.DEVICE_STORAGE

        // Stub test cursor
        Shadows.shadowOf(ApplicationProvider.getApplicationContext<Context>().contentResolver)
            .setCursor(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor)

        tracksStore = DeviceTracksStore(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            errorHandler
        )
    }

    @Test
    fun getAllDeviceTracksSortedByDate_WhenQueried() {
        // Given
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE
            )

        // When
        tracksStore.getAll(AudioTracksSorting.DateAdded(false)).test()

        // Then
        assertThat(cursor.closeWasCalled).isTrue()
        assertThat(cursor.sortOrder).isEqualTo("${MediaStore.Audio.Media.DATE_MODIFIED} DESC")
        assertThat(cursor.selection).isNull()
        assertThat(cursor.selectionArgs).isNull()
        assertThat(cursor.projection.toSet()).isEqualTo(projection.toSet())
        assertThat(cursor.uri).isEqualTo(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)

        // When
        tracksStore.getAll(AudioTracksSorting.DateAdded(true)).test()

        // Then
        assertThat(cursor.closeWasCalled).isTrue()
        assertThat(cursor.sortOrder).isEqualTo("${MediaStore.Audio.Media.DATE_MODIFIED} ASC")
        assertThat(cursor.selection).isNull()
        assertThat(cursor.selectionArgs).isNull()
        assertThat(cursor.projection.toSet()).isEqualTo(projection.toSet())
        assertThat(cursor.uri).isEqualTo(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
    }

    @Test
    fun getAllDeviceTracksSortedByArtist_WhenQueried() {
        // Given
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE
        )

        // When
        tracksStore.getAll(AudioTracksSorting.ArtistName(false)).test()

        // Then
        assertThat(cursor.closeWasCalled).isTrue()
        assertThat(cursor.sortOrder).isEqualTo("${MediaStore.Audio.Media.ARTIST} DESC")
        assertThat(cursor.selection).isNull()
        assertThat(cursor.selectionArgs).isNull()
        assertThat(cursor.projection.toSet()).isEqualTo(projection.toSet())
        assertThat(cursor.uri).isEqualTo(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)

        // When
        tracksStore.getAll(AudioTracksSorting.ArtistName(true)).test()

        // Then
        assertThat(cursor.closeWasCalled).isTrue()
        assertThat(cursor.sortOrder).isEqualTo("${MediaStore.Audio.Media.ARTIST} ASC")
        assertThat(cursor.selection).isNull()
        assertThat(cursor.selectionArgs).isNull()
        assertThat(cursor.projection.toSet()).isEqualTo(projection.toSet())
        assertThat(cursor.uri).isEqualTo(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
    }
}