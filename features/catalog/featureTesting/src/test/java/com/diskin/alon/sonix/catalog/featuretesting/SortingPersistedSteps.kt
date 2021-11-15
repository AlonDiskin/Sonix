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
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkConstructor
import io.reactivex.subjects.BehaviorSubject
import org.robolectric.Shadows

class SortingPersistedSteps : GreenCoffeeSteps() {

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

    @And("^User select other \"([^\"]*)\" and \"([^\"]*)\"$")
    fun user_select_other_something_and_something(sorting: String, order: String) {
        when(sorting) {
            "date" -> {
                when(order) {
                    "ascending" -> selectTracksUiOrderedSorting(TestSorting.ASC_DATE)
                    "descending" -> selectTracksUiOrderedSorting(TestSorting.DESC_DATE)
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

    @And("^Relaunch browser screen$")
    fun relaunch_browser_screen() {
        scenario.recreate()
    }

    @Then("^Tracks listing should shown sorted by \"([^\"]*)\" in \"([^\"]*)\" order$")
    fun tracks_listing_should_shown_sorted_by_something_in_something_order(sorting: String, order: String) {
        when(sorting) {
            "date" -> {
                when(order) {
                    "ascending" -> verifyTestDeviceTracksShow(deviceTracks[TestSorting.ASC_DATE]!!)
                    "descending" -> verifyTestDeviceTracksShow(deviceTracks[TestSorting.DESC_DATE]!!)
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
}