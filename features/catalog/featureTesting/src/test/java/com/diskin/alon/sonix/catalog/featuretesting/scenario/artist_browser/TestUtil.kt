package com.diskin.alon.sonix.catalog.featuretesting.scenario.artist_browser

import android.database.MatrixCursor
import android.os.Looper
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.presentation.R
import org.robolectric.Shadows

data class DeviceArtist(val id: Long,val name: String)

fun createDeviceArtists(): List<DeviceArtist> = listOf(
    DeviceArtist(1,"artist_1"),
    DeviceArtist(2,"artist_2"),
    DeviceArtist(3,"artist_3")
)

fun createArtistsMediaStoreCursor(artists: List<DeviceArtist>): MatrixCursor {
    val cursor = MatrixCursor(
        arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST
        ),
        artists.size
    )

    artists.forEach{ cursor.addRow(arrayOf(it.id,it.name))}
    return cursor
}

fun openArtistsSortingMenu() {
    openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    onView(withText(R.string.title_sort_artists_menu))
        .perform(ViewActions.click())
    Shadows.shadowOf(Looper.getMainLooper()).idle()
}