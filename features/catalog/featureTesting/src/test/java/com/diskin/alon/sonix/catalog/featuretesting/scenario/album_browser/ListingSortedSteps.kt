package com.diskin.alon.sonix.catalog.featuretesting.scenario.album_browser

import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import android.widget.RelativeLayout
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumsAdapter
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumsFragment
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.isRecyclerViewItemsCount
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

class ListingSortedSteps(
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val albums = createDeviceAlbums()
    private lateinit var expected: List<DeviceAlbum>

    init {
        // Stub mock contentResolver
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Albums.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        }
        val albumColumns = arrayOf(
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )

        every { contentResolver.query(contentUri,albumColumns,null,null,any()) } answers {
            val albums = when(val sort = args[4] as String) {
                "${MediaStore.Audio.Albums.ARTIST} ASC" -> albums.sortedBy(DeviceAlbum::artist)
                "${MediaStore.Audio.Albums.ARTIST} DESC" -> albums.sortedByDescending(DeviceAlbum::artist)
                "${MediaStore.Audio.Albums.ALBUM} ASC" -> albums.sortedBy(DeviceAlbum::name)
                "${MediaStore.Audio.Albums.ALBUM} DESC" -> albums.sortedByDescending(DeviceAlbum::name)
                else -> throw IllegalArgumentException("Unknown sort for media store test query:${sort}")
            }

            createAlbumMediaStoreCursor(albums)
        }

        every { contentResolver.registerContentObserver(contentUri,any(),any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
    }

    @Given("^user has never sorted albums listing before$")
    fun user_has_never_sorted_albums_listing_before() {
        clearSharedPrefs()
    }

    @And("^he open album browser$")
    fun he_open_album_browser() {
        scenario = launchFragmentInHiltContainer<AlbumsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^browser should list all albums by album name,in ascending order$")
    fun browser_should_list_all_albums_by_album_name_in_ascending_order() {
        expected = albums.sortedBy(DeviceAlbum::name)
        verifyExpectedAlbumsListingShown()
    }

    @When("^user sort albums by \"([^\"]*)\" in \"([^\"]*)\"$")
    fun user_sort_albums_by_something_in_something(sorting: String,order: String) {
        // Open sorting menu
        openAlbumsSortingMenu()

        // Select test sorting
        when(sorting) {
            "name" -> {
                when(order) {
                    "descending" -> {
                        albums.sortedByDescending(DeviceAlbum::name)
                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_ascending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                    }

                    else -> throw IllegalArgumentException("Unknown scenario argument:${order}")
                }
            }

            "artist" -> {
                onView(
                    allOf(
                        hasDescendant(withText(R.string.title_action_sort_artist_name)),
                        instanceOf(RelativeLayout::class.java)
                    )
                )
                    .perform(click())
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                openAlbumsSortingMenu()

                when(order) {
                    "ascending" -> {
                        expected = albums.sortedBy(DeviceAlbum::artist)
                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_ascending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                    }

                    "descending" -> {
                        expected = albums.sortedByDescending(DeviceAlbum::artist)
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

        // Block the testing thread so that list adapter thread could finish, currently no known sync method
        // other the block with sleep
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(3000)
    }

    @Then("^browser should list albums accordingly$")
    fun browser_should_list_albums_accordingly() {
        verifyExpectedAlbumsListingShown()
    }

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()

        assertThat(prefs.all.size).isEqualTo(0)
    }

    private fun verifyExpectedAlbumsListingShown() {
        onView(withId(R.id.albums))
            .check(ViewAssertions.matches(isRecyclerViewItemsCount(expected.size)))
        expected.forEachIndexed { index, album ->
            onView(withId(R.id.albums))
                .perform(
                    scrollToPosition<AlbumsAdapter.AlbumViewHolder>(
                        index
                    )
                )
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.albums)
                    .atPositionOnView(index, R.id.album_name)
            )
                .check(ViewAssertions.matches(withText(album.name)))

            onView(
                withRecyclerView(R.id.albums)
                    .atPositionOnView(index, R.id.album_artist)
            )
                .check(ViewAssertions.matches(withText(album.artist)))
        }
    }
}