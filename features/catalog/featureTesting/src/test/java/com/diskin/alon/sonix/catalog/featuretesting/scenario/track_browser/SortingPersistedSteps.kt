package com.diskin.alon.sonix.catalog.featuretesting.scenario.track_browser

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.os.Looper
import android.provider.MediaStore
import android.widget.RelativeLayout
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.data.TracksSortingStoreImpl
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.robolectric.Shadows

class SortingPersistedSteps(
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private lateinit var expectedSortingPref: String
    private var expectedAscOrderPref: Boolean = false

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
            0
        )

        every { contentResolver.query(any(),any(),any(),any(),any()) } returns cursor
        every { contentResolver.registerContentObserver(any(),any(),any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
    }

    @Given("^user opened tracks browser$")
    fun user_opened_tracks_browser() {
        // Launch audio tracks fragment
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^he select other \"([^\"]*)\" and \"([^\"]*)\"$")
    fun he_select_other_something_and_something(sorting: String, order: String) {
        openTracksSortingUiMenu()
        when(sorting) {
            "date" -> {
                expectedSortingPref = TracksSortingStoreImpl.TRACK_DATE_SORTING
                when(order) {
                    "ascending" -> {
                        expectedAscOrderPref = true
                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_date_added)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                        Shadows.shadowOf(Looper.getMainLooper()).idle()

                        openTracksSortingUiMenu()

                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_ascending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())

                    }
                    "descending" -> {
                        expectedAscOrderPref = false
                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_date_added)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                        Shadows.shadowOf(Looper.getMainLooper()).idle()

                        openTracksSortingUiMenu()

                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_descending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                    }
                    else -> throw IllegalArgumentException("Unknown scenario argument:$order")
                }
            }

            "artist name" -> {
                expectedSortingPref = TracksSortingStoreImpl.TRACK_ARTIST_SORTING
                when(order) {
                    "descending" -> {
                        expectedAscOrderPref = false

                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_artist_name)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                        Shadows.shadowOf(Looper.getMainLooper()).idle()

                        openTracksSortingUiMenu()

                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_descending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                    }
                    "ascending" -> {
                        expectedAscOrderPref = true

                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_artist_name)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                        Shadows.shadowOf(Looper.getMainLooper()).idle()

                        openTracksSortingUiMenu()

                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_ascending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                    }
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

    @And("^leave app$")
    fun leave_app() {
        scenario.onActivity { it.finish() }
    }

    @Then("^tracks listing should shown sorted by \"([^\"]*)\" in \"([^\"]*)\" order$")
    fun tracks_listing_should_shown_sorted_by_something_in_something_order(sorting: String, order: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val actualOrderPref = prefs.getBoolean(TracksSortingStoreImpl.KEY_ORDER,false)
        val actualSortingPref = prefs.getString(TracksSortingStoreImpl.KEY_SORTING,null)

        assertThat(actualOrderPref).isEqualTo(expectedAscOrderPref)
        assertThat(actualSortingPref).isEqualTo(expectedSortingPref)
    }

    private fun openTracksSortingUiMenu() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_tracks_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }
}