package com.diskin.alon.sonix.catalog.featuretesting

import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import com.diskin.alon.sonix.catalog.data.DeviceTracksStore
import com.diskin.alon.sonix.catalog.events.SelectedPlaylist
import com.diskin.alon.sonix.catalog.events.SelectedPlaylistPublisher
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.verify
import io.reactivex.Observable
import org.robolectric.Shadows

class PlayAudioSelectionSteps(
    private val selectedPlaylistPublisher: SelectedPlaylistPublisher
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val deviceTracks = createTestDeviceTracks()[TestSorting.DESC_DATE]!!

    init {
        every { selectedPlaylistPublisher.publish(any()) } returns Unit
    }

    @Given("^User has public audio tracks on device$")
    fun user_has_public_audio_tracks_on_device() {
        mockkConstructor(DeviceTracksStore::class)
        every { anyConstructed<DeviceTracksStore>().getAll(any()) } returns
                Observable.just(AppResult.Success(deviceTracks))
    }

    @When("^User open device tracks browser screen$")
    fun user_open_device_tracks_browser_screen() {
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^User select first track$")
    fun user_select_first_track() {
        onView(withRecyclerView(R.id.tracks).atPosition(0))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App player should play first track as part of all tracks play queue$")
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