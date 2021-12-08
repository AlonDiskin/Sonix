package com.diskin.alon.sonix.catalog.infrastructure

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.catalog.events.SelectedPlaylistPublisher
import com.diskin.alon.sonix.common.application.AppResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [30])
class PlaylistSenderImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var sender: PlaylistSenderImpl

    // Collaborators
    private val playlistPublisher: SelectedPlaylistPublisher = mockk()

    @Before
    fun setUp() {
        sender = PlaylistSenderImpl(playlistPublisher)
    }

    @Test
    fun publishPlaylistSelectionEvent_WhenListSent() {
        // Given
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val ids = listOf(1,2,3,4,5,6)
        val startIndex = 2
        val expectedEvent = SelectedPlaylist(
            startIndex,
            ids.map { id ->
                Uri.parse(
                    contentUri
                        .toString()
                        .plus("/$id")
                )
            }
        )

        every { playlistPublisher.publish(any()) } returns Unit

        // When
        val observer = sender.send(startIndex, ids).test()

        // Then
        verify { playlistPublisher.publish(expectedEvent) }
        observer.assertValue(AppResult.Success(Unit))
    }
}