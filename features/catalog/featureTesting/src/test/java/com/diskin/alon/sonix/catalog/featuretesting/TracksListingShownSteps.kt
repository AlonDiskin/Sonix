package com.diskin.alon.sonix.catalog.featuretesting

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.core.AudioTrack
import com.diskin.alon.sonix.catalog.data.DeviceTracksStore
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkConstructor
import io.reactivex.subjects.BehaviorSubject
import org.robolectric.Shadows

class TracksListingShownSteps : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private lateinit var tracksSubject: BehaviorSubject<AppResult<List<AudioTrack>>>
    private val deviceTracks: Map<TestSorting, List<AudioTrack>> = createTestDeviceTracks()

    @Given("^User has public audio tracks on device$")
    fun user_has_public_audio_tracks_on_device() {
        // Stub test tracks
        mockkConstructor(DeviceTracksStore::class)
        every { anyConstructed<DeviceTracksStore>().getAll(AudioTracksSorting.DateAdded(true)) } answers {
            tracksSubject = BehaviorSubject.createDefault(AppResult.Success(deviceTracks[TestSorting.ASC_DATE]!!))
            tracksSubject
        }
        every { anyConstructed<DeviceTracksStore>().getAll(AudioTracksSorting.DateAdded(false)) } answers {
            tracksSubject = BehaviorSubject.createDefault(AppResult.Success(deviceTracks[TestSorting.DESC_DATE]!!))
            tracksSubject
        }
        every { anyConstructed<DeviceTracksStore>().getAll(AudioTracksSorting.ArtistName(true)) } answers {
            tracksSubject = BehaviorSubject.createDefault(AppResult.Success(deviceTracks[TestSorting.ASC_ARTIST]!!))
            tracksSubject
        }
        every { anyConstructed<DeviceTracksStore>().getAll(AudioTracksSorting.ArtistName(false)) } answers {
            tracksSubject = BehaviorSubject.createDefault(AppResult.Success(deviceTracks[TestSorting.DESC_ARTIST]!!))
            tracksSubject
        }
    }

    @When("^User open audio browser screen$")
    fun user_open_audio_browser_screen() {
        // Launch audio tracks fragment
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Device public tracks should be listed by date, in descending order$")
    fun device_public_tracks_should_be_listed_by_date_in_descending_order() {
        // Verify expected tracks are shown in ui
        verifyTestDeviceTracksShow(deviceTracks[TestSorting.DESC_DATE]!!)
    }

    @When("^User select other \"([^\"]*)\" and \"([^\"]*)\"$")
    fun user_select_other_sorting_and_order(sorting: String, order: String) {
        when(sorting) {
            "date" -> {
                when(order) {
                    "ascending" -> selectTracksUiOrderedSorting(TestSorting.ASC_DATE)
                    else -> throw IllegalArgumentException("Unknown scenario argument:$order")
                }
            }

            "artist name" -> {
                when(order) {
                    "descending" -> selectTracksUiOrderedSorting(TestSorting.DESC_ARTIST)
                    "ascending" -> selectTracksUiOrderedSorting(TestSorting.ASC_ARTIST)
                    else -> throw IllegalArgumentException("Unknown scenario argument:$order")
                }
            }

            else -> throw IllegalArgumentException("Unknown scenario argument:$sorting")
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Block the testing thread so that list adapter thread could finish, currently no known sync method
        // other the block with sleep
        Thread.sleep(2000)
    }

    @Then("^Tracks listing should be sorted by \"([^\"]*)\" in order \"([^\"]*)\"$")
    fun tracks_listing_should_be_sorted_and_ordered_as_selected(sorting: String, order: String) {
        when(sorting) {
            "date" -> {
                when(order) {
                    "ascending" -> verifyTestDeviceTracksShow(deviceTracks[TestSorting.ASC_DATE]!!)
                    else -> throw IllegalArgumentException("Unknown scenario argument:$order")
                }
            }

            "artist name" -> {
                when(order) {
                    "descending" -> verifyTestDeviceTracksShow(deviceTracks[TestSorting.DESC_ARTIST]!!)
                    "ascending" -> verifyTestDeviceTracksShow(deviceTracks[TestSorting.ASC_ARTIST]!!)
                    else -> throw IllegalArgumentException("Unknown scenario argument:$order")
                }
            }

            else -> throw IllegalArgumentException("Unknown scenario argument:$sorting")
        }
    }

    @When("^First listed track deleted from device$")
    fun first_listed_track_deleted_from_device() {
        val updatedList = (tracksSubject.value as AppResult.Success<List<AudioTrack>>).data.toMutableList()

        updatedList.removeAt(0)
        tracksSubject.onNext(AppResult.Success(updatedList))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(2000)
    }

    @Then("^Tracks listing should be updated accordingly$")
    fun tracks_listing_should_be_updated_accordingly() {
        verifyTestDeviceTracksShow((tracksSubject.value as AppResult.Success<List<AudioTrack>>).data)
    }
}