package com.diskin.alon.sonix.catalog.featuretesting

import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.core.AudioTrack
import com.diskin.alon.sonix.catalog.data.DeviceTracksStore
import com.diskin.alon.sonix.catalog.presentation.R
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
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog

class TrackDeletedSteps : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val deviceTracks = createTestDeviceTracks()[TestSorting.DESC_DATE]!!
    private val tracksSubject: BehaviorSubject<AppResult<List<AudioTrack>>> =
        BehaviorSubject.createDefault(AppResult.Success(deviceTracks))

    @Given("^User has public audio tracks on device$")
    fun user_has_public_audio_tracks_on_device() {
        mockkConstructor(DeviceTracksStore::class)
        every { anyConstructed<DeviceTracksStore>().getAll(any()) } returns tracksSubject
        every { anyConstructed<DeviceTracksStore>().delete(deviceTracks.first().id) } answers {
            val updatedTracks = deviceTracks.toMutableList()

            updatedTracks.removeAt(0)
            tracksSubject.onNext(AppResult.Success(updatedTracks))
            Single.just(AppResult.Success(Unit))
        }
    }

    @When("^User open audio browser screen$")
    fun user_open_audio_browser_screen() {
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^User select to delete first and last listed tracks$")
    fun user_select_to_delete_first_and_last_listed_tracks() {
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView((withText(R.string.title_action_delete_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Block the testing thread so that list adapter thread could finish, currently no known sync method
        // other the block with sleep
        Thread.sleep(2000)
    }

    @Then("^App should delete selected tracks from device$")
    fun app_should_delete_selected_tracks_from_device() {
        verify { anyConstructed<DeviceTracksStore>().delete(deviceTracks.first().id) }
    }

    @And("^App should update shown listed tracks$")
    fun app_should_update_shown_listed_tracks() {
        verifyTestDeviceTracksShow((tracksSubject.value!! as AppResult.Success<List<AudioTrack>>).data)
    }
}