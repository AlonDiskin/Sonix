package com.diskin.alon.sonix.catalog.featuretesting.scenario.track_browser

import android.content.ContentResolver
import android.database.MatrixCursor
import android.os.Looper
import android.provider.MediaStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTrackDetailDialog
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchDialogInHiltContainer
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import org.apache.commons.lang3.time.DurationFormatUtils
import org.robolectric.Shadows

class TrackDetailShownSteps(
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    private val deviceTracks = createDeiceTracks()

    init {
        // Stub mock content resolver
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
            ),
            deviceTracks.size
        )

        deviceTracks.forEach {
            cursor.addRow(
                arrayOf(
                    it.id,
                    it.name,
                    it.album,
                    it.artist,
                    it.format,
                    it.size,
                    it.path,
                    it.duration
                )
            )
        }

        every { contentResolver.query(any(),any(),any(),any(),any()) } returns cursor
        every { contentResolver.registerContentObserver(any(),any(),any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit

        // Set test nav controller
        navController.setGraph(R.navigation.catalog_nav_graph)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.audioTrackDetailDialog) {
                launchDialogInHiltContainer<AudioTrackDetailDialog>(
                    navController.currentBackStackEntry!!.arguments
                )
                { AudioTrackDetailDialog() }
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
        }
    }

    @Given("^user open audio browser screen$")
    fun user_open_audio_browser_screen() {
        // Launch audio tracks fragment
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragments.first()
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^user select to view track detail of first track$")
    fun user_select_to_view_track_detail_of_first_track() {
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView((withText(R.string.title_action_track_detail)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^app should show track detail for first track$")
    fun app_should_show_track_detail_for_first_track() {
        val track = deviceTracks.first()
        val trackDuration = DurationFormatUtils.formatDuration(
            track.duration,
            "mm:ss",
            true
        )

        onView(withId(R.id.track_artist))
            .inRoot(isDialog())
            .check(matches(withText(track.artist)))
        onView(withId(R.id.track_album))
            .inRoot(isDialog())
            .check(matches(withText(track.album)))
        onView(withId(R.id.track_path))
            .inRoot(isDialog())
            .check(matches(withText(track.path)))
        onView(withId(R.id.track_duration))
            .inRoot(isDialog())
            .check(matches(withText(trackDuration)))
        onView(withId(R.id.track_format))
            .inRoot(isDialog())
            .check(matches(withText(track.format)))
    }
}