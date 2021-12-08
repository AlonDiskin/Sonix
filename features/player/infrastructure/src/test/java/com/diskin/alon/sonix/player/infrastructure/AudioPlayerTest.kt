package com.diskin.alon.sonix.player.infrastructure

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.sonix.common.exoplayertestutil.TestExoPlayerBuilder
import com.diskin.alon.sonix.player.infrastructure.model.AudioPlayerTrack
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkConstructor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * [AudioPlayer] unit test class.
 */
@RunWith(AndroidJUnit4::class)
class AudioPlayerTest {

    // Test subject
    private lateinit var player: AudioPlayer

    // Stub data
    private val testPlayer = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext())
        .build()

    @Before
    fun setUp() {
        // Set mock player
        mockkConstructor(ExoPlayer.Builder::class)
        every { anyConstructed<ExoPlayer.Builder>().build() } returns testPlayer

        // Init subject
        player = AudioPlayer(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun closePlayer_WhenReleased() {
        // Given

        // When
        player.release()

        // Then
        // TODO find how to verify player release
    }

    @Test
    fun setPlaylistAndPlayTrack_WhenPlayTracksOfNewList() {
        // Given
        val  playlist = listOf(
            Uri.fromFile(File("src/test/resources/track_1.mp3")),
            Uri.fromFile(File("src/test/resources/track_2.mp3"))
        )

        // When
        player.playTracks(1,playlist)
        TestPlayerRunHelper.runUntilPlaybackState(testPlayer,Player.STATE_READY)

        // Then
        val uri = mutableListOf<Uri>()
        for (i in 0 until testPlayer.mediaItemCount) {
            testPlayer.getMediaItemAt(i).localConfiguration?.let {
                uri.add(it.uri)
            }
        }

        assertThat(uri).isEqualTo(playlist)
        assertThat(testPlayer.currentMediaItem?.localConfiguration?.uri).isEqualTo(playlist[1])
        assertThat(testPlayer.isPlaying).isTrue()
    }

    @Test
    fun updateCurrentTrack_WhenPlayingSelectedPlaylist() {
        // Given
        val  playlist = listOf(
            Uri.fromFile(File("src/test/resources/track_1.mp3")),
            Uri.fromFile(File("src/test/resources/track_2.mp3"))
        )

        // When
        player.playTracks(0,playlist)
        TestPlayerRunHelper.runUntilPlaybackState(testPlayer,Player.STATE_READY)

        // Then
        assertThat(player.currentTrack.value).isEqualTo(AudioPlayerTrack(playlist.first(),true))
    }

    @Test
    fun updateCurrentTrack_WhenPlayingNextTrack() {
        // Given
        val  playlist = listOf(
            Uri.fromFile(File("src/test/resources/track_1.mp3")),
            Uri.fromFile(File("src/test/resources/track_2.mp3"))
        )

        // When
        player.playTracks(0,playlist)
        TestPlayerRunHelper.runUntilPlaybackState(testPlayer,Player.STATE_ENDED)

        // Then
        assertThat(player.currentTrack.value).isEqualTo(AudioPlayerTrack(playlist[1],true))
    }

    @Test
    fun playNextTrack_WhenFailToLoadTrackFile() {
        // Given
        val  playlist = listOf(
            Uri.fromFile(File("src/test/resources/non_existing_track.mp3")),
            Uri.fromFile(File("src/test/resources/track_1.mp3"))
        )

        testPlayer.addMediaItem(MediaItem.fromUri(playlist[0]))
        testPlayer.addMediaItem(MediaItem.fromUri(playlist[1]))
        testPlayer.prepare()

        // When
        testPlayer.play()
        TestPlayerRunHelper.runUntilPlaybackState(testPlayer,Player.STATE_READY)

        // Then
        assertThat(testPlayer.currentMediaItem?.localConfiguration?.uri).isEqualTo(playlist[1])
    }

    @Test
    fun removeFailedTrackFromPlaylist_WhenFailToLoadTrackFile() {
        // Given
        val  playlist = listOf(
            Uri.fromFile(File("src/test/resources/non_existing_track.mp3")),
            Uri.fromFile(File("src/test/resources/track_1.mp3"))
        )

        testPlayer.addMediaItem(MediaItem.fromUri(playlist[0]))
        testPlayer.addMediaItem(MediaItem.fromUri(playlist[1]))
        testPlayer.prepare()

        // When
        testPlayer.play()
        TestPlayerRunHelper.runUntilPlaybackState(testPlayer,Player.STATE_READY)

        // Then
        assertThat(testPlayer.mediaItemCount).isEqualTo(1)
    }

    @Test
    fun playFirstTrack_WhenFailToLoadLastTrackFile() {
        // Given
        val playlist = listOf(
            Uri.fromFile(File("src/test/resources/track_1.mp3")),
            Uri.fromFile(File("src/test/resources/non_existing_track.mp3"))
        )

        testPlayer.addMediaItem(MediaItem.fromUri(playlist[0]))
        testPlayer.addMediaItem(MediaItem.fromUri(playlist[1]))
        testPlayer.prepare()
        testPlayer.seekTo(1,0)

        // When
        testPlayer.play()
        TestPlayerRunHelper.runUntilPlaybackState(testPlayer,Player.STATE_READY)

        // Then
        assertThat(testPlayer.currentMediaItemIndex).isEqualTo(0)
        assertThat(testPlayer.currentMediaItem?.localConfiguration?.uri).isEqualTo(playlist[0])
    }

    @Test
    fun updatePlayerHasNoTrack_WhenFailTLoadSingleTrack() {
        // Given
        testPlayer.addMediaItem(MediaItem.fromUri(Uri.fromFile(File("src/test/resources/non_existing_track.mp3"))))
        testPlayer.prepare()

        // When
        testPlayer.play()
        TestPlayerRunHelper.runUntilError(testPlayer)

        // Then
        assertThat(player.currentTrack.value).isNull()
    }

    @Test
    fun playCurrentTrack_WhenPlayInvoked() {
        // Given
        testPlayer.addMediaItem(MediaItem.fromUri(Uri.fromFile(File("src/test/resources/track_1.mp3"))))
        testPlayer.prepare()
        TestPlayerRunHelper.runUntilPlaybackState(testPlayer,Player.STATE_READY)

        // When
        player.play()

        // Then
        assertThat(testPlayer.isPlaying).isTrue()
    }

    @Test
    fun pauseCurrentTrack_WhenPauseInvoked() {
        // Given
        testPlayer.addMediaItem(MediaItem.fromUri(Uri.fromFile(File("src/test/resources/track_1.mp3"))))
        testPlayer.prepare()
        testPlayer.play()
        TestPlayerRunHelper.runUntilPlaybackState(testPlayer,Player.STATE_READY)

        // When
        player.pause()

        // Then
        assertThat(testPlayer.isPlaying).isFalse()
    }
}