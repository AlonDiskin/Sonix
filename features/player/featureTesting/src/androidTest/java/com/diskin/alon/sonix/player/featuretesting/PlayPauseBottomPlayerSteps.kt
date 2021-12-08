package com.diskin.alon.sonix.player.featuretesting

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
import com.diskin.alon.sonix.player.featuretesting.util.EspressoIdlingResource
import com.diskin.alon.sonix.player.featuretesting.util.WhiteBox
import com.diskin.alon.sonix.player.infrastructure.AudioPlayer
import com.diskin.alon.sonix.player.presentation.PlayerFragment
import com.diskin.alon.sonix.player.presentation.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.common.truth.Truth
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class PlayPauseBottomPlayerSteps(
    private val playlistProvider: SelectedPlayListProvider,
    private val contentResolver: ContentResolver,
    player: AudioPlayer
) : GreenCoffeeSteps() {

    private lateinit var scenario: FragmentScenario<PlayerFragment>
    private val playListSubject = BehaviorSubject.create<SelectedPlaylist>()
    private val deviceTrack = Pair(
        Uri.fromFile(File("//android_asset/audio/track_1.mp3")),
        arrayOf("title_1", "artist_1", "album_1", 12000L)
    )
    private val exoPlayer = WhiteBox.getInternalState(player,"exoPlayer") as ExoPlayer

    init {
        every { playlistProvider.get() } returns playListSubject
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        exoPlayer.setThrowsWhenUsingWrongThread(false)
    }

    @Given("^User has track on device$")
    @Throws(Throwable::class)
    fun user_has_track_on_device() {
        val columns = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        every { contentResolver.query(deviceTrack.first,columns,null,null,null)
        } returns createMetadataCursor(deviceTrack.second)
    }

    @When("^User select to play track$")
    fun user_select_to_play_track() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Sonix)

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    EspressoIdlingResource.decrement()
                }
            }
        })
        EspressoIdlingResource.increment()
        playListSubject.onNext(SelectedPlaylist(0, listOf(deviceTrack.first)))
        waitForUiThread()
    }

    @Then("^Player should play track and display pause button$")
    fun player_should_play_track_and_display_pause_button() {
        checkTrackPlaying(deviceTrack.first)
        scenario.onFragment{
            val button = it.requireView().findViewById<ImageButton>(R.id.play_pause_button)
            val tag = it.requireContext().getString(R.string.tag_pause)
            assertThat(button.tag).isEqualTo(tag)
        }
    }

    @When("^User press the pause button$")
    fun user_press_the_pause_button() {
        onView(withId(R.id.play_pause_button))
            .perform(click())
    }

    @Then("^Player should pause track and display play button$")
    fun player_should_pause_track_and_display_play_button() {
        assertThat(exoPlayer.isPlaying).isFalse()
        scenario.onFragment{
            val button = it.requireView().findViewById<ImageButton>(R.id.play_pause_button)
            val tag = it.requireContext().getString(R.string.tag_play)
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

    private fun waitForUiThread() {
        while (!EspressoIdlingResource.isIdle()) { Thread.sleep(500) }
    }

    private fun checkTrackPlaying(uri: Uri) {
        assertThat(exoPlayer.isPlaying).isTrue()
        assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri).isEqualTo(uri)
    }
}