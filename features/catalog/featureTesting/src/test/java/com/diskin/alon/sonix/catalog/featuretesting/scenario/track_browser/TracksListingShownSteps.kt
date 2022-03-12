package com.diskin.alon.sonix.catalog.featuretesting.scenario.track_browser

import android.content.ContentResolver
import android.database.MatrixCursor
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import android.widget.RelativeLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksAdapter
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher
import com.diskin.alon.sonix.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.robolectric.Shadows

class TracksListingShownSteps(
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val deviceTracks = createDeiceTracks()
    private lateinit var expected: List<DeviceTrack>

    init {
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val selection = "${MediaStore.Audio.Media.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("audio/mpeg")
        val audioColumns = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )

        every { contentResolver.query(contentUri,audioColumns,selection,selectionArgs,any()) } answers {
            val tracks = when(val sort = args[4] as String) {
                "${MediaStore.Audio.Media.DATE_MODIFIED} ASC" -> deviceTracks
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC" -> deviceTracks.reversed()
                "${MediaStore.Audio.Media.ARTIST} ASC" -> deviceTracks.sortedBy(DeviceTrack::artist)
                "${MediaStore.Audio.Media.ARTIST} DESC" -> deviceTracks.sortedByDescending(DeviceTrack::artist)
                else -> throw IllegalArgumentException("Unknown sort for media store test query:${sort}")
            }

            createAudioMediaStoreCursor(
                tracks.map {
                    arrayOf(it.id,it.name,it.album,it.artist,it.format,it.size,it.path,it.duration)
                }
            )
        }
        every { contentResolver.registerContentObserver(any(),any(),any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
    }

    @Given("^user opened audio browser screen$")
    fun user_open_audio_browser_screen() {
        // Launch audio tracks fragment
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^device public tracks should be listed by date, in descending order$")
    fun device_public_tracks_should_be_listed_by_date_in_descending_order() {
        // Verify expected tracks are shown in ui
        verifyDeviceTracksShow(deviceTracks.reversed())
    }

    @When("^user select other \"([^\"]*)\" and \"([^\"]*)\"$")
    fun user_select_other_sorting_and_order(sorting: String, order: String) {
        openTracksSortingUiMenu()
        when(sorting) {
            "date" -> {
                when(order) {
                    "ascending" -> {
                        expected = deviceTracks

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
                    else -> throw IllegalArgumentException("Unknown scenario argument:$order")
                }
            }

            "artist name" -> {
                when(order) {
                    "descending" -> {
                        expected = deviceTracks.sortedByDescending(DeviceTrack::artist)

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
                        expected = deviceTracks.sortedBy(DeviceTrack::artist)

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

    @Then("^tracks listing should be sorted by \"([^\"]*)\" in order \"([^\"]*)\"$")
    fun tracks_listing_should_be_sorted_and_ordered_as_selected(sorting: String, order: String) {
        verifyExpectedTracksListingShown()
    }

    private fun createAudioMediaStoreCursor(values: List<Array<out Any>>): MatrixCursor {
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
            values.size
        )

        values.forEach(cursor::addRow)
        return cursor
    }

    private fun verifyExpectedTracksListingShown() {
        onView(withId(R.id.tracks))
            .check(ViewAssertions.matches(isRecyclerViewItemsCount(expected.size)))
        expected.forEachIndexed { index, track ->
            onView(withId(R.id.tracks))
                .perform(
                    RecyclerViewActions.scrollToPosition<AudioTracksAdapter.AudioTrackViewHolder>(
                        index
                    )
                )
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                RecyclerViewMatcher.withRecyclerView(R.id.tracks)
                    .atPositionOnView(index, R.id.track_name)
            )
                .check(ViewAssertions.matches(withText(track.name)))

            onView(
                RecyclerViewMatcher.withRecyclerView(R.id.tracks)
                    .atPositionOnView(index, R.id.track_artist)
            )
                .check(ViewAssertions.matches(withText(track.artist)))
        }
    }
}