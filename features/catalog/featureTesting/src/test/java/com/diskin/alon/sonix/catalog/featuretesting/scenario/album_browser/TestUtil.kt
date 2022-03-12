package com.diskin.alon.sonix.catalog.featuretesting.scenario.album_browser

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.presentation.R
import org.robolectric.Shadows

fun openAlbumsSortingMenu() {
    openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    onView(withText(R.string.title_sort_albums_menu))
        .perform(click())
    Shadows.shadowOf(Looper.getMainLooper()).idle()
}