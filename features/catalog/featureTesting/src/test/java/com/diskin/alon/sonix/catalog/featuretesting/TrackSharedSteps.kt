package com.diskin.alon.sonix.catalog.featuretesting

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.core.AudioTrack
import com.diskin.alon.sonix.catalog.data.DeviceTracksStore
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkConstructor
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.robolectric.Shadows

class TrackSharedSteps : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val deviceTracks = createTestDeviceTracks()[TestSorting.DESC_DATE]!!

    @Given("^User has public audio tracks on device$")
    fun user_has_public_audio_tracks_on_device() {
        mockkConstructor(DeviceTracksStore::class)
        every { anyConstructed<DeviceTracksStore>().getAll(any()) } returns
                Observable.just(AppResult.Success(deviceTracks))
    }

    @When("^User open audio browser screen$")
    fun user_open_audio_browser_screen() {
        // Launch audio tracks fragment
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()

        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Intents.init()
    }

    @And("^select to share first listed track$")
    fun select_to_share_first_listed_track() {
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView((withText(R.string.title_action_share_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should show device sharing ui$")
    fun app_should_show_device_sharing_ui() {
        val firstTrackUri = Uri.parse(
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            .toString().plus("/${deviceTracks.first().id}"))

        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        Intents.intended(IntentMatchers.hasExtraWithKey(Intent.EXTRA_INTENT))

        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.type).isEqualTo(
            ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.mime_type_audio))
        assertThat(intent.clipData!!.getItemAt(0).uri).isEqualTo(firstTrackUri)

        Intents.release()
    }
}