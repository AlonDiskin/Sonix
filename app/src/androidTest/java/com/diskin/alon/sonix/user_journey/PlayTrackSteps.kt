package com.diskin.alon.sonix.user_journey

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.R
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.withImageButtonTag
import com.diskin.alon.sonix.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.hamcrest.CoreMatchers.allOf

class PlayTrackSteps : GreenCoffeeSteps() {

    val deviceTracks = DeviceUtil.copyAudioFilesToDevice(
        listOf(
            "assets/audio/Broke_For_Free_01_Night_Owl.mp3",
            "assets/audio/DR_03_Sedativa_III.mp3",
            "assets/audio/JekK_Day_Free.mp3",
            "assets/audio/Sia_The_Greatest.mp3",
            "assets/audio/StrangeZero_AirBook.mp3",
            "assets/audio/The.madpix.project_Bad_Chick.mp3",
        )
    )

    @Given("^user launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
    }

    @When("^he select to play first listed device track$")
    fun he_select_to_play_first_listed_device_track() {
        onView(
            withRecyclerView(R.id.tracks).atPosition(0)
        )
            .perform(click())
    }

    @Then("^app should play track$")
    fun app_should_play_track() {
        Thread.sleep(4000)
        // verify against last track since default sorting list last added to device as first
        val track = deviceTracks.last()
        val pauseTag = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.tag_pause)

        onView(withId(R.id.player_root))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(allOf(withId(R.id.track_name), withParent(withId(R.id.player_root))))
            .check(matches(withText(track.title)))
        onView(allOf(withId(R.id.track_artist), withParent(withId(R.id.player_root))))
            .check(matches(withText(track.artist)))
        onView(withId(R.id.play_pause_button))
            .check(matches(withImageButtonTag(pauseTag)))
    }
}