package com.diskin.alon.sonix.catalog.featuretesting.scenario.track_browser

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksAdapter.AudioTrackViewHolder
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.isRecyclerViewItemsCount
import org.robolectric.Shadows

data class DeviceTrack(val id: Long,
                       val path: String,
                       val name: String,
                       val artist: String,
                       val album: String,
                       val size: Long,
                       val duration: Long,
                       val format: String)

fun createDeiceTracks(): List<DeviceTrack> = listOf(
    DeviceTrack(1,"path_1","title_1","artist_1","album_1",20000L,40000L,"audio/mp3"),
    DeviceTrack(2,"path_2","title_2","artist_2","album_2",10000L,50000L,"audio/mp3"),
    DeviceTrack(3,"path_3","title_3","artist_3","album_3",30000L,20000L,"audio/mp3"),
    DeviceTrack(4,"path_3","title_4","artist_4","album_4",50000L,30000L,"audio/mp3")
)

fun verifyDeviceTracksShow(tracks: List<DeviceTrack>) {
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