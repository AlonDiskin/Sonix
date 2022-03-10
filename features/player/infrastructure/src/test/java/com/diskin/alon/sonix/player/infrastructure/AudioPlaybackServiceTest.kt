package com.diskin.alon.sonix.player.infrastructure

import android.app.Notification
import android.app.PendingIntent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.media.MediaBrowserServiceCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import io.mockk.*
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [AudioPlaybackService] unit test class.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = HiltTestApplication::class)
class AudioPlaybackServiceTest {

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
    @BindValue @JvmField
    val notificationFactory: AudioNotificationFactory = mockk()

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
        //Shadows.shadowOf(Looper.getMainLooper()).idle()
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
        Shadows.shadowOf(Looper.getMainLooper()).idle()

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
        val playerTrack = AudioPlayerTrack(Uri.EMPTY,false,30L)
        val metadata = createMetadata()
        val playbackState = PlaybackStateCompat.STATE_PAUSED
        val sessionMock = mockk<MediaSessionCompat>()
        val slotMetadata = slot<MediaMetadataCompat>()
        val slotPlaybackState = slot<PlaybackStateCompat>()

        every { sessionMock.setMetadata(capture(slotMetadata)) } returns Unit
        every { sessionMock.setPlaybackState(capture(slotPlaybackState)) } returns Unit
        service.mediaSession = sessionMock

        // When
        playerCurrentTrack.value = playerTrack
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { metadataStore.get(playerTrack.uri) }

        // When
        metadataSubject.onSuccess(AppResult.Success(metadata))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE)).isEqualTo(metadata.name)
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)).isEqualTo(metadata.artist)
        assertThat(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)).isEqualTo(metadata.album)
        assertThat(slotMetadata.captured.bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)).isEqualTo(metadata.duration)
        assertThat(Uri.parse(slotMetadata.captured.bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))).isEqualTo(metadata.uri)
        assertThat(slotPlaybackState.captured.state).isEqualTo(playbackState)
        assertThat(slotPlaybackState.captured.position).isEqualTo(playerTrack.position)
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
        playerCurrentTrack.value = AudioPlayerTrack(mockk(),false,0L)
        metadataSubject.onSuccess(AppResult.Error(storeError))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(storeError.name).isEqualTo(slot.captured.getString(KEY_SERVICE_ERROR))
    }

    @Test
    fun playTrack_WhenMediaControllerPassPlayRequest() {
        // Given
        every { player.play() } returns Unit

        // When
        service.sessionCallback.onPlay()

        // Then
        verify { player.play() }
    }

    @Test
    fun pauseTrack_WhenMediaControllerPassPauseRequest() {
        // Given
        every { player.pause() } returns Unit

        // When
        service.sessionCallback.onPause()

        // Then
        verify { player.pause() }
    }

    @Test
    fun skipNextTrack_WhenMediaControllerPassSipNextRequest() {
        // Given
        every { player.skipNext() } returns Unit

        // When
        service.sessionCallback.onSkipToNext()

        // Then
        verify { player.skipNext() }
    }

    @Test
    fun skipPrevTrack_WhenMediaControllerPassSipPrevRequest() {
        // Given
        every { player.skipPrev() } returns Unit

        // When
        service.sessionCallback.onSkipToPrevious()

        // Then
        verify { player.skipPrev() }
    }

    @Test
    fun seekToTrackPlaybackPosition_WhenMediaControllerPassSeekRequest() {
        // Given
        val position = 20L
        every { player.seek(position) } returns Unit

        // When
        service.sessionCallback.onSeekTo(position)

        // Then
        verify { player.seek(position) }
    }

    @Test
    fun stopService_WhenMediaControllerPassStopRequest() {
        // Given

        // When
        service.sessionCallback.onStop()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(Shadows.shadowOf(service).isStoppedBySelf).isTrue()
    }

    @Test
    fun showPlayerNotificationForPlayedPlayback_WhenPlayerPlayTrack() {
        // Given
        val track = AudioPlayerTrack(Uri.EMPTY,true,120L)
        val metadata = TrackMetadata("artist","artist","album",300L, Uri.EMPTY)
        val pendingIntent = mockk<PendingIntent>()
        val controller = mockk<MediaControllerCompat>()
        val notification = mockk<Notification>()

        every { notificationFactory.buildPlayedNotification(any(),any(),any()) } returns notification
        every { controller.sessionActivity } returns pendingIntent
        WhiteBox.setInternalState(service.mediaSession,"mController",controller)
        metadataSubject.onSuccess(AppResult.Success(metadata))

        // When
        playerCurrentTrack.value = track
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify {
            notificationFactory.buildPlayedNotification(
                metadata,
                service.mediaSession.sessionToken,
                pendingIntent
            )
        }
        assertThat(Shadows.shadowOf(service).lastForegroundNotification).isEqualTo(notification)
    }

    @Test
    fun showPlayerNotificationForPausedPlayback_WhenPlayerPauseTrack() {
        // Given
        val track = AudioPlayerTrack(Uri.EMPTY,false,120L)
        val metadata = TrackMetadata("artist","artist","album",300L, Uri.EMPTY)
        val controller = mockk<MediaControllerCompat>()
        val pendingIntent = mockk<PendingIntent>()
        val notification = mockk<Notification>()

        every { notificationFactory.buildPausedNotification(any(),any(),any()) } returns notification
        every { controller.sessionActivity } returns pendingIntent
        WhiteBox.setInternalState(service.mediaSession,"mController",controller)
        metadataSubject.onSuccess(AppResult.Success(metadata))

        mockkConstructor(NotificationManagerCompat::class)
        every { anyConstructed<NotificationManagerCompat>().notify(any(),any()) } returns Unit

        // When
        playerCurrentTrack.value = track
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify {
            notificationFactory.buildPausedNotification(
                metadata,
                service.mediaSession.sessionToken,
                pendingIntent
            )
        }
        verify { anyConstructed<NotificationManagerCompat>().notify(NOTIFICATION_ID,notification) }
    }

    @Test
    fun doNotShowNotification_WhenPlayerTrackRestored() {
        // Given
        val track = AudioPlayerTrack(Uri.EMPTY,false,120L,true)
        val metadata = TrackMetadata("","","",300L, Uri.EMPTY)

        metadataSubject.onSuccess(AppResult.Success(metadata))

        // When
        playerCurrentTrack.value = track
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 0) { notificationFactory.buildPausedNotification(any(),any(),any()) }
        verify(exactly = 0) { notificationFactory.buildPlayedNotification(any(),any(),any()) }
    }

    @Test
    fun startService_WhenPlaybackIsPlayed() {
        // Given
        every { player.play() } returns Unit

        // When
        service.sessionCallback.onPlay()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(service.started).isTrue()
    }

    @Test
    fun doNotRecreateService_WhenKilled() {
        // Given

        // When
        val actual = service.onStartCommand(null,0,0)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(actual).isEqualTo(MediaBrowserServiceCompat.START_NOT_STICKY)
    }

    @Test
    fun startForeGround_WhenShowPlayNotification() {
        // Given
        val track = AudioPlayerTrack(Uri.EMPTY,true,120L)
        val metadata = TrackMetadata("artist","artist","album",300L, Uri.EMPTY)
        val pendingIntent = mockk<PendingIntent>()
        val controller = mockk<MediaControllerCompat>()

        every { controller.sessionActivity } returns pendingIntent
        WhiteBox.setInternalState(service.mediaSession,"mController",controller)
        metadataSubject.onSuccess(AppResult.Success(metadata))

        // When
        playerCurrentTrack.value = track
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(Shadows.shadowOf(service).isForegroundStopped).isFalse()
    }

    @Test
    fun stopForeGround_WhenShowPauseNotification() {
        // Given
        val track = AudioPlayerTrack(Uri.EMPTY,false,120L)
        val metadata = TrackMetadata("artist","artist","album",300L, Uri.EMPTY)
        val controller = mockk<MediaControllerCompat>()
        val pendingIntent = mockk<PendingIntent>()
        val notification = mockk<Notification>()

        every { notificationFactory.buildPausedNotification(any(),any(),any()) } returns notification
        every { controller.sessionActivity } returns pendingIntent
        WhiteBox.setInternalState(service.mediaSession,"mController",controller)
        metadataSubject.onSuccess(AppResult.Success(metadata))

        mockkConstructor(NotificationManagerCompat::class)
        every { anyConstructed<NotificationManagerCompat>().notify(any(),any()) } returns Unit

        // When
        playerCurrentTrack.value = track
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(Shadows.shadowOf(service).isForegroundStopped).isTrue()
    }
}