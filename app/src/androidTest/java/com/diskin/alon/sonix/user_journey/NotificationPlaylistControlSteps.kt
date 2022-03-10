package com.diskin.alon.sonix.user_journey

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.uiautomator.UiSelector
import com.diskin.alon.sonix.R
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.*
import com.diskin.alon.sonix.player.infrastructure.AudioPlaybackService
import com.diskin.alon.sonix.util.DeviceUtil
import com.diskin.alon.sonix.util.WhiteBox
import com.google.android.exoplayer2.ExoPlayer
import com.google.common.truth.Truth.*
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class NotificationPlaylistControlSteps : GreenCoffeeSteps() {

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

    @Given("^user is listening to selected playlist$")
    fun user_is_listening_to_selected_playlist() {
        // Launch app from  device home screen
        DeviceUtil.launchAppFromHome()
        // Select first track
        onView(withRecyclerView(R.id.tracks).atPosition(0))
            .perform(click())
    }

    @When("^he exist app$")
    fun he_exist_app() {
        DeviceUtil.pressBack()
        Thread.sleep(3000)
    }

    @And("^open app player notification$")
    fun open_app_player_notification() {
        DeviceUtil.openNotifications()
    }

    @When("^user pause and skip to next track$")
    fun user_pause_and_skip_to_next_track() {
        val  context = ApplicationProvider.getApplicationContext<Context>()
        DeviceUtil.getDevice().findObject(
            UiSelector()
            .description(context.getString(R.string.title_notification_pause)))
            .click()
        DeviceUtil.getDevice().findObject(
            UiSelector()
                .description(context.getString(R.string.title_notification_skip_next)))
            .click()
        Thread.sleep(3000)
    }

    @Then("^app player should skip to next track in playlist and pause playback$")
    fun app_player_should_skip_to_next_track_in_playlist_and_pause_playback() {
        val service = AudioPlaybackService.service!!
        val player = service.player
        val exoPlayer = WhiteBox.getInternalState(player,"exoPlayer") as ExoPlayer

        exoPlayer.setThrowsWhenUsingWrongThread(false)
        assertThat(exoPlayer.currentMediaItem?.localConfiguration?.uri)
            .isEqualTo(deviceTracks[deviceTracks.lastIndex - 1].uri)
        assertThat(exoPlayer.isPlaying).isFalse()

        // Close notification
        DeviceUtil.pressBack()
    }

}