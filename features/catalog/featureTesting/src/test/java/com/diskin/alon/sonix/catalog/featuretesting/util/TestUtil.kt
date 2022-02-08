package com.diskin.alon.sonix.catalog.featuretesting.util

import android.os.Looper
import android.widget.RelativeLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.catalog.core.AudioTrack
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksAdapter.AudioTrackViewHolder
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.isRecyclerViewItemsCount
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.robolectric.Shadows

enum class TestSorting { DESC_DATE,ASC_DATE,DESC_ARTIST,ASC_ARTIST }

fun createTestDeviceTracks() = mapOf(
    Pair(
        TestSorting.DESC_DATE,
        listOf(
            AudioTrack(1,"path_1","title_1","artist_1","album_1",20000L,40000L,"audio/mp3"),
            AudioTrack(2,"path_2","title_2","artist_2","album_2",10000L,50000L,"audio/mp3"),
            AudioTrack(3,"path_3","title_3","artist_3","album_3",30000L,20000L,"audio/mp3"),
            AudioTrack(4,"path_3","title_4","artist_4","album_4",50000L,30000L,"audio/mp3")
        )
    ),
    Pair(
        TestSorting.ASC_DATE,
        listOf(
            AudioTrack(3,"path_3","title_3","artist_3","album_3",30000L,20000L,"audio/mp3"),
            AudioTrack(1,"path_1","title_1","artist_1","album_1",20000L,40000L,"audio/mp3"),
            AudioTrack(4,"path_3","title_4","artist_4","album_4",50000L,30000L,"audio/mp3"),
            AudioTrack(2,"path_2","title_2","artist_2","album_2",10000L,50000L,"audio/mp3"),
        )),
    Pair(
        TestSorting.DESC_ARTIST,
        listOf(
            AudioTrack(4,"path_3","title_4","artist_4","album_4",50000L,30000L,"audio/mp3"),
            AudioTrack(3,"path_3","title_3","artist_3","album_3",30000L,20000L,"audio/mp3"),
            AudioTrack(2,"path_2","title_2","artist_2","album_2",10000L,50000L,"audio/mp3"),
            AudioTrack(1,"path_1","title_1","artist_1","album_1",20000L,40000L,"audio/mp3")
        )),
    Pair(
        TestSorting.ASC_ARTIST,
        listOf(
            AudioTrack(4,"path_3","title_4","artist_4","album_4",50000L,30000L,"audio/mp3"),
            AudioTrack(2,"path_2","title_2","artist_2","album_2",10000L,50000L,"audio/mp3"),
            AudioTrack(1,"path_1","title_1","artist_1","album_1",20000L,40000L,"audio/mp3"),
            AudioTrack(3,"path_3","title_3","artist_3","album_3",30000L,20000L,"audio/mp3"),
        ))
)

fun verifyTestDeviceTracksShow(tracks: List<AudioTrack>) {
    onView(withId(R.id.tracks))
        .check(matches(isRecyclerViewItemsCount(tracks.size)))

    tracks.forEachIndexed { index, track ->
        onView(withId(R.id.tracks))
            .perform(
                scrollToPosition<AudioTrackViewHolder>(
                    index
                )
            )
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(
            withRecyclerView(R.id.tracks)
                .atPositionOnView(index, R.id.track_name)
        )
            .check(matches(withText(track.name)))

        onView(
            withRecyclerView(R.id.tracks)
                .atPositionOnView(index, R.id.track_artist)
        )
            .check(matches(withText(track.artist)))
    }
}

fun openTracksSortingUiMenu() {
    openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
    onView(withText(R.string.title_sort_tracks_menu))
        .perform(click())
    Shadows.shadowOf(Looper.getMainLooper()).idle()
}

fun selectTracksUiOrderedSorting(orderedSorting: TestSorting) {
    openTracksSortingUiMenu()
    when(orderedSorting) {
        TestSorting.ASC_DATE -> {
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
        TestSorting.DESC_DATE -> {
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

        TestSorting.ASC_ARTIST -> {
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

        TestSorting.DESC_ARTIST -> {
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
    }
}