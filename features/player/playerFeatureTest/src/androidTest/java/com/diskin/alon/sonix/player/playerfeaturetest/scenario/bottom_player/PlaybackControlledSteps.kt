package com.diskin.alon.sonix.player.playerfeaturetest.scenario.bottom_player

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageButton
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.diskin.alon.sonix.catalog.events.SelectedPlayListProvider
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.player.infrastructure.AudioPlaybackService
import com.diskin.alon.sonix.player.playerfeaturetest.util.EspressoIdlingResource
import com.diskin.alon.sonix.player.playerfeaturetest.util.WhiteBox
import com.diskin.alon.sonix.player.presentation.PlayerFragment
import com.diskin.alon.sonix.player.presentation.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class PlaybackControlledSteps(
    private val playlistProvider: SelectedPlayListProvider,
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: FragmentScenario<PlayerFragment>
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var expectedButtonState: String
    private val playListSubject = BehaviorSubject.create<SelectedPlaylist>()
    private val deviceTrack = Pair(
        Uri.fromFile(File("//android_asset/audio/track_1.mp3")),
        arrayOf("title_1", "artist_1", "album_1", 12000L)
    )

    init {
        // Stub mocked collaborators
        val columns = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )

        every { contentResolver.query(deviceTrack.first,columns,null,null,null)
        } returns createMetadataCursor(deviceTrack.second)
        every { playlistProvider.get() } returns playListSubject

        // Register espresso idling resource
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Given("^user selected a track$")
    fun user_selected_a_track() {
        // Launch player fragment
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)

        // Get exoPLayer instance from service audio player
        waitForAudioServiceCreation()
        val service = AudioPlaybackService.service!!
        exoPlayer = WhiteBox.getInternalState(service.player,"exoPlayer") as ExoPlayer
        exoPlayer.setThrowsWhenUsingWrongThread(false)

        // Add listener to player to monitor idling resource and select track to start playback
        service.player.currentTrack.observeForever {
            if (!EspressoIdlingResource.isIdle()) {
                EspressoIdlingResource.decrement()
            }
        }
        EspressoIdlingResource.increment()
        playListSubject.onNext(SelectedPlaylist(0, listOf(deviceTrack.first)))
        waitForIdlingResource()
    }

    @When("^he pause the track playback$")
    fun he_pause_the_track_playback() {
        onView(withId(R.id.play_pause_button))
            .perform(click())
    }

    @Then("^player should pause the playback$")
    fun player_should_pause_the_track() {
        assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri)
            .isEqualTo(deviceTrack.first)
        assertThat(exoPlayer.isPlaying).isFalse()

        scenario.onFragment{
            val button = it.requireView().findViewById<ImageButton>(R.id.play_pause_button)
            val tag = it.requireContext().getString(R.string.tag_play)

            assertThat(button.tag).isEqualTo(tag)
        }
    }

    @When("^play the track again$")
    fun play_the_track_again() {
        Thread.sleep(2000)
        onView(withId(R.id.play_pause_button))
            .perform(click())
    }

    @Then("^player should resume the playback$")
    fun player_should_resume_the_track() {
        assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri)
            .isEqualTo(deviceTrack.first)
        assertThat(exoPlayer.isPlaying).isTrue()

        scenario.onFragment{
            val button = it.requireView().findViewById<ImageButton>(R.id.play_pause_button)
            val tag = it.requireContext().getString(R.string.tag_pause)

            assertThat(button.tag).isEqualTo(tag)
        }
    }

    private fun createMetadataCursor(values: Array<out Any>?): MatrixCursor {
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION
            ),
            1
        )

        cursor.addRow(values)
        return cursor
    }

    private fun waitForIdlingResource() {
        while (!EspressoIdlingResource.isIdle()) { Thread.sleep(500) }
    }

    private fun waitForAudioServiceCreation() {
        scenario.onFragment {
            while (!it.mediaBrowser.isConnected) {
                Thread.sleep(500)
            }
        }
    }
}