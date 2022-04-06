package com.diskin.alon.sonix.catalog.featuretesting.scenario.artist_browser

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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.ArtistsAdapter
import com.diskin.alon.sonix.catalog.presentation.controller.ArtistsFragment
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
    private val artists = createDeviceArtists()
    private lateinit var expected: List<DeviceArtist>

    init {
        // Stub mock contentResolver
        val artistsContentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Artists.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        }
        val artistColumns = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST
        )
        val albumsContentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Albums.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        }

        every { contentResolver.query(artistsContentUri,artistColumns,null,null,any()) } answers {
            val artists = when(val sort = args[4] as String) {
                "${MediaStore.Audio.Artists.ARTIST} ASC" -> artists.sortedBy(DeviceArtist::name)
                "${MediaStore.Audio.Artists.ARTIST} DESC" -> artists.sortedByDescending(DeviceArtist::name)
                "${MediaStore.Audio.Artists.DEFAULT_SORT_ORDER} ASC" -> artists
                "${MediaStore.Audio.Artists.DEFAULT_SORT_ORDER} DESC" -> artists.reversed()
                else -> throw IllegalArgumentException("Unknown sort for media store test query:${sort}")
            }

            createArtistsMediaStoreCursor(artists)
        }

        every { contentResolver.registerContentObserver(artistsContentUri,any(),any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit
        every { contentResolver.query(albumsContentUri,any(),any(),any(),any()) } answers {
            val cursor = MatrixCursor(
                arrayOf(MediaStore.Audio.Albums.ALBUM_ID),
                1
            )

            cursor.addRow(arrayOf(1L))
            cursor
        }
    }

    @Given("^user has never sorted artists listing before$")
    fun user_has_never_sorted_artists_listing_before() {
        clearSharedPrefs()
    }

    @And("^he open artist browser$")
    fun he_open_artist_browser() {
        scenario = launchFragmentInHiltContainer<ArtistsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^browser should list all artists by name,in ascending order$")
    fun browser_should_list_all_artists_by_name_in_ascending_order() {
        expected = artists.sortedBy(DeviceArtist::name)
        verifyExpectedArtistsListingShown()
    }

    @When("^user sort artists by \"([^\"]*)\" in \"([^\"]*)\"$")
    fun user_sort_artists_by_something_in_something(sorting: String,order: String) {
        // Open sorting menu
        openArtistsSortingMenu()

        // Select test sorting
        when(sorting) {
            "name" -> {
                when(order) {
                    "descending" -> {
                        artists.sortedByDescending(DeviceArtist::name)
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

            "date" -> {
                onView(
                    allOf(
                        hasDescendant(withText(R.string.title_action_sort_date_added)),
                        instanceOf(RelativeLayout::class.java)
                    )
                )
                    .perform(click())
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                openArtistsSortingMenu()

                when(order) {
                    "ascending" -> {
                        expected = artists
                        onView(
                            allOf(
                                hasDescendant(withText(R.string.title_action_sort_ascending)),
                                instanceOf(RelativeLayout::class.java)
                            )
                        )
                            .perform(click())
                    }

                    "descending" -> {
                        expected = artists.reversed()
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

    @Then("^browser should list artists accordingly$")
    fun browser_should_list_artists_accordingly() {
        verifyExpectedArtistsListingShown()
    }

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()

        assertThat(prefs.all.size).isEqualTo(0)
    }

    private fun verifyExpectedArtistsListingShown() {
        onView(withId(R.id.artists))
            .check(matches(isRecyclerViewItemsCount(expected.size)))
        expected.forEachIndexed { index, artist ->
            onView(withId(R.id.artists))
                .perform(
                    scrollToPosition<ArtistsAdapter.ArtistViewHolder>(
                        index
                    )
                )
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.artists)
                    .atPositionOnView(index, R.id.artist_name)
            )
                .check(matches(withText(artist.name)))
        }
    }
}