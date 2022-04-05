package com.diskin.alon.sonix.user_journey

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.R
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.util.DeviceUtil
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class PlayAlbumSteps : GreenCoffeeSteps() {

    val deviceTracks = DeviceUtil.copyAudioFilesToDevice(
        listOf(
            "assets/audio/DR_03_Sedativa_III.mp3",
            "assets/audio/DR_05_Sedativa_V.mp3",
            "assets/audio/DR_04_Sedativa_IV.mp3"
        )
    )

    @Given("^user launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
    }

    @When("^he open albums browser$")
    fun he_open_albums_browser() {
        onView(withText("ALBUMS"))
            .perform(click())
    }

    @And("^select an album from albums browser$")
    fun select_an_album_from_albums_browser() {
        onView(
            withRecyclerView(R.id.albums).atPosition(0)
        )
            .perform(click())
    }

    @And("^select to play whole album$")
    fun select_to_play_whole_album() {
        onView(withContentDescription("play album tracks"))
    }

    @Then("^app should play album as playlist from first track$")
    fun app_should_play_album_as_playlist_from_first_track() {
    }
}