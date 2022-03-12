package com.diskin.alon.sonix.catalog.featuretesting.scenario.album_browser

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
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.sonix.catalog.data.AlbumsSortingStoreImpl
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumsFragment
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

class SortingSavedSteps(
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private lateinit var expectedSortingPref: String
    private var expectedAscOrderPref: Boolean = false

    init {
        // Stub mock contentResolver
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.Audio.Albums.ALBUM_ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
            ),
            0
        )

        every { contentResolver.query(any(),any(),any(),any(),any()) } returns cursor
        every { contentResolver.registerContentObserver(any(),any(),any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
    }

    @Given("^user opened albums browser$")
    fun user_opened_albums_browser() {
        scenario = launchFragmentInHiltContainer<AlbumsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @When("^he sort albums by \"([^\"]*)\" in \"([^\"]*)\"$")
    fun he_sort_albums_by_something_in_something(sorting: String, order: String) {
        openAlbumsSortingMenu()
        when(sorting) {
            "name" -> {
                expectedSortingPref = AlbumsSortingStoreImpl.ALBUM_NAME_SORTING
                when (order) {
                    "descending" -> {
                        expectedAscOrderPref = false
                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_descending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                    }

                    else -> throw IllegalArgumentException("Unknown scenario argument:${order}")
                }
            }

            "artist" -> {
                expectedSortingPref = AlbumsSortingStoreImpl.ARTIST_SORTING
                onView(
                    allOf(
                        hasDescendant(withText(R.string.title_action_sort_artist_name)),
                        instanceOf(RelativeLayout::class.java)
                    )
                )
                    .perform(click())
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                openAlbumsSortingMenu()

                when (order) {
                    "ascending" -> {
                        expectedAscOrderPref = true
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
                                hasDescendant(withText(R.string.title_action_sort_descending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                    }

                    else -> throw IllegalArgumentException("Unknown scenario argument:${order}")
                }
            }

            else -> throw IllegalArgumentException("Unknown scenario argument:${sorting}")
        }
    }

    @And("^he leaves app$")
    fun he_leaves_app() {
        scenario.onActivity { it.finish() }
    }

    @Then("^browser should save user selected sorting$")
    fun browser_should_save_user_selected_sorting() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val actualOrderPref = prefs.getBoolean(AlbumsSortingStoreImpl.KEY_ORDER,false)
        val actualSortingPref = prefs.getString(AlbumsSortingStoreImpl.KEY_SORTING,null)

        assertThat(actualOrderPref).isEqualTo(expectedAscOrderPref)
        assertThat(actualSortingPref).isEqualTo(expectedSortingPref)
    }
}