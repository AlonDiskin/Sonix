package com.diskin.alon.sonix.user_journey

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksAdapter
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.sonix.util.DeviceUtil
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When

class EngageTrackOptionsSteps : GreenCoffeeSteps() {

    lateinit var deviceTracks: List<DeviceUtil.DeviceTrack>

    @Given("^User has public audio tracks on his device$")
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

    @When("^User select to view the detail of the first catalog track$")
    fun user_select_to_view_the_detail_of_the_first_catalog_track() {
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        onView((withText(R.string.title_action_track_detail)))
            .perform(click())
    }

    @Then("^App should show track detail to user$")
    fun app_should_show_track_detail_to_user() {
        val track = deviceTracks.last()

        onView(withId(R.id.track_artist))
            .inRoot(isDialog())
            .check(matches(withText(track.artist)))
        onView(withId(R.id.track_album))
            .inRoot(isDialog())
            .check(matches(withText(track.album)))
        onView(withId(R.id.track_path))
            .inRoot(isDialog())
            .check(matches(withText(track.path)))
    }

    @When("^Use select to share track$")
    fun use_select_to_share_track() {
        Intents.init()
        DeviceUtil.pressBack()

        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        onView((withText(R.string.title_action_share_track)))
            .perform(click())

    }

    @Then("^App should show device sharing menu$")
    fun app_should_should_show_device_sharing_menu() {
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        Intents.intended(IntentMatchers.hasExtraWithKey(Intent.EXTRA_INTENT))

        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.type).isEqualTo(
            ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.mime_type_audio))
        assertThat(intent.clipData!!.getItemAt(0).uri).isEqualTo(deviceTracks.last().uri)

        Intents.release()
        DeviceUtil.pressBack()
    }

    @When("^User select to delete track$")
    fun user_select_to_delete_track() {
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        onView((withText(R.string.title_action_delete_track)))
            .perform(click())
        onView(withText("OK"))
            .inRoot(isDialog())
            .perform(click())
    }

    @Then("^App should delete track from device$")
    fun app_should_delete_track_from_device() {
        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(deviceTracks.last().uri.lastPathSegment)
        val cursor = ApplicationProvider.getApplicationContext<Context>()
            .contentResolver.query(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                },
                arrayOf(MediaStore.Audio.Media._ID),
                selection,
                selectionArgs,
                null
            )!!

        assertThat(cursor.count).isEqualTo(0)
        cursor.close()
    }

    @And("^Update tracks listing accordingly$")
    fun update_tracks_listing_accordingly() {
        val temp = deviceTracks.toMutableList()
        temp.removeAt(deviceTracks.size - 1)
        deviceTracks = temp

        onView(withId(R.id.tracks))
            .check(matches(isRecyclerViewItemsCount(deviceTracks.size)))
        deviceTracks.reversed().forEachIndexed { index, track ->
            onView(withId(R.id.tracks))
                .perform(
                    RecyclerViewActions.scrollToPosition<AudioTracksAdapter.AudioTrackViewHolder>(
                        index
                    )
                )

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