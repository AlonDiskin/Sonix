package com.diskin.alon.sonix.catalog.featuretesting.scenario.track_browser

import android.content.ContentResolver
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.catalog.events.SelectedPlaylistPublisher
import com.diskin.alon.sonix.catalog.featuretesting.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.verify
import org.robolectric.Shadows

class PlayAudioSelectionSteps(
    private val selectedPlaylistPublisher: SelectedPlaylistPublisher,
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val deviceTracks = createDeiceTracks()

    init {
        // Stub mock playlist sender
        every { selectedPlaylistPublisher.publish(any()) } returns Unit
    }

    @Given("^user has public audio tracks on device$")
    fun user_has_public_audio_tracks_on_device() {
        // Create test tracks by stubbing mock content resolver
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
            ),
            deviceTracks.size
        )

        deviceTracks.forEach {
            cursor.addRow(
                arrayOf(
                    it.id,
                    it.name,
                    it.album,
                    it.artist,
                    it.format,
                    it.size,
                    it.path,
                    it.duration
                )
            )
        }

        every { contentResolver.query(any(),any(),any(),any(),any()) } returns cursor
        every { contentResolver.registerContentObserver(any(),any(),any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
    }

    @When("^user open device tracks browser screen$")
    fun user_open_device_tracks_browser_screen() {
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^user select first track$")
    fun user_select_first_track() {
        onView(withRecyclerView(R.id.tracks).atPosition(0))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app player should play first track as part of all tracks play queue$")
    fun app_player_should_play_first_track_as_part_of_all_tracks_play_queue() {
        val audioCollectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val expectedSelected = SelectedPlaylist(
            0,
            deviceTracks.map { track ->
                Uri.parse(
                    audioCollectionUri.toString()
                        .plus("/${track.id}")
                )
            }
        )

        verify { selectedPlaylistPublisher.publish(expectedSelected) }
    }
}