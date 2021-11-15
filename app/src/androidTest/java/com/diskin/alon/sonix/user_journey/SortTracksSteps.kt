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

    lateinit var testTracks: List<DeviceUtil.DeviceTrack>

    @Given("^User has not changed tracks sorting$")
    fun user_has_not_changed_tracks_sorting() {
        DeviceUtil.clearSharedPrefs()
    }

    @And("^User has public audio tracks on his device$")
    fun user_has_public_audio_tracks_on_his_device() {
        testTracks = DeviceUtil.copyAudioFilesToDevice(
            listOf(
                "assets/audio/Elvis Presley - If I Can Dream '68.mp3",
                "assets/audio/The Beatles - Yesterday.mp3",
                "assets/audio/Led Zeppelin - Black Dog (Official Audio).mp3",
                "assets/audio/The Doors - The End (Live At the Isle Of Wight 1970).mp3",
                "assets/audio/The Jimi Hendrix Experience - All Along The Watchtower (Official Audio).mp3",
                "assets/audio/The Rolling Stones - Honky Tonk Woman (Brussels Affair, Live in 1973).mp3",
            )
        )
    }

    @When("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
    }

    @Then("^App should show tracks catalog sorted by date in desc order$")
    fun app_should_show_tracks_catalog_sorted_by_date_in_desc_order() {
        onView(withId(R.id.tracks))
            .check(matches(isRecyclerViewItemsCount(testTracks.size)))
        testTracks.reversed().forEachIndexed { index, track ->
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
            .check(matches(isRecyclerViewItemsCount(testTracks.size)))
        testTracks.forEachIndexed { index, track ->
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