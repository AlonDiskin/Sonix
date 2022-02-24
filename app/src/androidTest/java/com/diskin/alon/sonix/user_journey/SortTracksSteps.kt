package com.diskin.alon.sonix.user_journey

import android.widget.RelativeLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksAdapter.AudioTrackViewHolder
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.sonix.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf

class SortTracksSteps : GreenCoffeeSteps() {

    lateinit var deviceTracks: List<DeviceUtil.DeviceTrack>

    @Given("^User has not changed tracks sorting$")
    fun user_has_not_changed_tracks_sorting() {
        DeviceUtil.clearSharedPrefs()
    }

    @And("^User has public audio tracks on his device$")
    fun user_has_public_audio_tracks_on_his_device() {
        deviceTracks = DeviceUtil.copyAudioFilesToDevice(
            listOf(
                "assets/audio/Broke_For_Free_01_Night_Owl.mp3",
                "assets/audio/DR_03_Sedativa_III.mp3",
                "assets/audio/JekK_Day_Free.mp3",
                "assets/audio/Sia_The_Greatest.mp3",
                "assets/audio/StrangeZero_AirBook.mp3",
                "assets/audio/The.madpix.project_Bad_Chick.mp3",
            )
        )
    }

    @When("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
    }

    @Then("^App should show tracks sorted by date in descending order$")
    fun app_should_show_tracks_sorted_by_date_in_descending_order() {
        onView(withId(R.id.tracks))
            .check(matches(isRecyclerViewItemsCount(deviceTracks.size)))
        deviceTracks.reversed().forEachIndexed { index, track ->
            onView(withId(R.id.tracks))
                .perform(scrollToPosition<AudioTrackViewHolder>(index))

            onView(
                withRecyclerView(R.id.tracks)
                    .atPositionOnView(index, R.id.track_name)
            )
                .check(matches(withText(track.title)))

            onView(
                withRecyclerView(R.id.tracks)
                    .atPositionOnView(index, R.id.track_artist)
            )
                .check(matches(withText(track.artist)))
        }
    }

    @When("^User select tracks ordering as ascending$")
    fun user_select_tracks_ordering_as_ascending() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_tracks_menu))
            .perform(click())
        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_ascending)),
                instanceOf(RelativeLayout::class.java)
            )
        )
            .perform(click())
    }

    @And("^User relaunch app$")
    fun user_relaunch_app() {
        DeviceUtil.pressBack()
        DeviceUtil.launchAppFromHome()
    }

    @Then("^App should show tracks catalog sorted by date in ascending order$")
    fun app_should_show_tracks_catalog_sorted_by_date_in_ascending_order() {
        onView(withId(R.id.tracks))
            .check(matches(isRecyclerViewItemsCount(deviceTracks.size)))
        deviceTracks.forEachIndexed { index, track ->
            onView(withId(R.id.tracks))
                .perform(scrollToPosition<AudioTrackViewHolder>(index))

            onView(
                withRecyclerView(R.id.tracks)
                    .atPositionOnView(index, R.id.track_name)
            )
                .check(matches(withText(track.title)))

            onView(
                withRecyclerView(R.id.tracks)
                    .atPositionOnView(index, R.id.track_artist)
            )
                .check(matches(withText(track.artist)))
        }
    }
}