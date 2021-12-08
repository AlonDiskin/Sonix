package com.diskin.alon.sonix.player.infrastructure

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.player.infrastructure.interfaces.TrackMetadataStore
import com.diskin.alon.sonix.player.infrastructure.model.AudioPlayerTrack
import com.diskin.alon.sonix.player.infrastructure.model.SingleLiveEvent
import com.diskin.alon.sonix.player.infrastructure.model.TrackMetadata
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [AudioPlaybackService] unit test class.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HiltTestApplication::class)
class AudioPlaybackServiceTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var service: AudioPlaybackService

    // Collaborators
    @BindValue @JvmField
    val player: AudioPlayer = mockk()
    @BindValue @JvmField
    val playListProvider: SelectedPlayListProvider = mockk()
    @BindValue @JvmField
    val metadataStore: TrackMetadataStore = mockk()

    // Stub data
    private val playerCurrentTrack = MutableLiveData<AudioPlayerTrack>()
    private val playListSubject = BehaviorSubject.create<SelectedPlaylist>()
    private val error = SingleLiveEvent<AppError>()
    private val metadataSubject = SingleSubject.create<AppResult<TrackMetadata>>()

    @Before
    fun setUp() {
        // Stub mocks
        every { player.currentTrack } returns playerCurrentTrack
        every { player.release() } returns Unit
        every { player.error } returns error
        every { playListProvider.get() } returns playListSubject
        every { metadataStore.get(any()) } returns metadataSubject

        // Start service under test
        service = Robolectric.setupService(AudioPlaybackService::class.java)
    }

    @Test
    fun returnEmptyBrowserRoot() {
        // Given

        // When
        val actual = service.onGetRoot("package_name",1,null)!!

        // Then
        assertThat(actual.extras).isNull()
        assertThat(actual.rootId).isEqualTo(MY_EMPTY_MEDIA_ROOT_ID)
    }

    @Test(expected = IllegalStateException::class)
    fun throwException_WhenBrowserChildrenLoadingRequested() {
        // Given

        // When
        service.onLoadChildren("id", mockk())
    }

    @Test
    fun startPlaylist_WhenSelected() {
        // Given
        val list = createPlaylist()

        every { player.playTracks(list.startIndex,list.tracks) } returns Unit

        // When
        playListSubject.onNext(list)

        // Then
        verify { player.playTracks(list.startIndex,list.tracks) }
    }

    @Test
    fun releasePlayer_WhenDestroyed() {
        // Given

        // When
        service.onDestroy()

        // Then
        verify { player.release() }
    }

    @Test
    fun releaseSubscriptions_WhenDestroyed() {
        // Given

        // When
        service.onDestroy()

        // Then
        assertThat(service.disposable.isDisposed).isTrue()
    }

    @Test
    fun updateSessionMetadataAndPlaybackState_WhenPlayerTrackChanged() {
        // Given
        val playerTrack = AudioPlayerTrack(Uri.EMPTY,false)
        val metadata = createMetadata()
        val playbackState = PlaybackStateCompat.STATE_PAUSED
        val playbackPosition = 0
        val sessionMock = mockk<MediaSessionCompat>()
        val slotMetadata = slot<MediaMetadataCompat>()
        val slotPlaybackState = slot<PlaybackStateCompat>()

        every { sessionMock.setMetadata(capture(slotMetadata)) } returns Unit
        every { sessionMock.setPlaybackState(capture(slotPlaybackState)) } returns Unit
        service.mediaSession = sessionMock

        // When
        playerCurrentTrack.value = playerTrack

        // Then
        verify { metadataStore.get(playerTrack.uri) }

        // When
        metadataSubject.onSuccess(AppResult.Success(metadata))

        // Then
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE)).isEqualTo(metadata.name)
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)).isEqualTo(metadata.artist)
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)).isEqualTo(metadata.album)
        assertThat(slotMetadata.captured.bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)).isEqualTo(metadata.duration)
        assertThat(Uri.parse(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))).isEqualTo(metadata.uri)
        assertThat(slotPlaybackState.captured.state).isEqualTo(playbackState)
        assertThat(slotPlaybackState.captured.position).isEqualTo(playbackPosition)
    }

    @Test
    fun doNotUpdateSessionMetadata_WhenPlayerUpdatesSameTrack() {
        // Given
        val playerTrack = AudioPlayerTrack(Uri.EMPTY,true)

        // When
        playerCurrentTrack.value = playerTrack
        playerCurrentTrack.value = playerTrack

        // Then
        verify(exactly = 1) { metadataStore.get(playerTrack.uri) }
        assertThat(service.mediaSession.controller.playbackState.state).isEqualTo(PlaybackStateCompat.STATE_PLAYING)
    }

    @Test
    fun updateSessionMetadataAndPlaybackState_WhenPlayerHasNoCurrentTrack() {
        // Given
        val playbackState = PlaybackStateCompat.STATE_NONE
        val emptyMetadata = TrackMetadata("","","",0L, Uri.EMPTY)
        val sessionMock = mockk<MediaSessionCompat>()
        val slotMetadata = slot<MediaMetadataCompat>()
        val slotPlaybackState = slot<PlaybackStateCompat>()

        every { sessionMock.setMetadata(capture(slotMetadata)) } returns Unit
        every { sessionMock.setPlaybackState(capture(slotPlaybackState)) } returns Unit
        service.mediaSession = sessionMock

        // When
        playerCurrentTrack.value = null

        // Then
        verify(exactly = 0) { metadataStore.get(any()) }
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE)).isEqualTo(emptyMetadata.name)
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)).isEqualTo(emptyMetadata.artist)
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)).isEqualTo(emptyMetadata.album)
        assertThat(slotMetadata.captured.bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)).isEqualTo(emptyMetadata.duration)
        assertThat(Uri.parse(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))).isEqualTo(emptyMetadata.uri)
        assertThat(slotPlaybackState.captured.state).isEqualTo(playbackState)
    }

    @Test
    fun updateServiceError_WhenPlayerFail() {
        // Given
        val playerError = AppError.PLAYER_ERROR
        val sessionMock = mockk<MediaSessionCompat>()
        val slot = slot<Bundle>()

        every { sessionMock.setExtras(capture(slot)) } returns Unit
        service.mediaSession = sessionMock

        // When
        error.value = playerError

        // Then
        assertThat(playerError.name).isEqualTo(slot.captured.getString(KEY_SERVICE_ERROR))
    }

    @Test
    fun updateServiceError_WhenMetadataFetchFail() {
        // Given
        val storeError = AppError.DEVICE_STORAGE
        val sessionMock = mockk<MediaSessionCompat>()
        val slot = slot<Bundle>()

        every { sessionMock.setExtras(capture(slot)) } returns Unit
        service.mediaSession = sessionMock

        // When
        playerCurrentTrack.value = AudioPlayerTrack(mockk(),false)
        metadataSubject.onSuccess(AppResult.Error(storeError))

        // Then
        assertThat(storeError.name).isEqualTo(slot.captured.getString(KEY_SERVICE_ERROR))
    }

    @Test
    fun playTrack_WhenControllerPassPlayRequest() {
        // Given
        every { player.play() } returns Unit

        // When
        service.sessionCallback.onPlay()

        // Then
        verify { player.play() }
    }

    @Test
    fun pauseTrack_WhenControllerPassPauseRequest() {
        // Given
        every { player.pause() } returns Unit

        // When
        service.sessionCallback.onPause()

        // Then
        verify { player.pause() }
    }
}