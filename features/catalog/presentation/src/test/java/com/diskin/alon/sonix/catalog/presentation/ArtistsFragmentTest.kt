package com.diskin.alon.sonix.catalog.presentation

import android.os.Looper
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumsAdapter
import com.diskin.alon.sonix.catalog.presentation.controller.ArtistsFragment
import com.diskin.alon.sonix.catalog.presentation.model.UiArtist
import com.diskin.alon.sonix.catalog.presentation.viewmodel.ArtistsViewModel
import com.diskin.alon.sonix.common.presentation.ImageLoader
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import io.mockk.*
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [ArtistsFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class ArtistsFragmentTest {

    // System under test
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: ArtistsViewModel = mockk()

    // Stub data
    private val artists = MutableLiveData<List<UiArtist>>()
    private val update = MutableLiveData<ViewUpdateState>()
    private val sorting = MutableLiveData<ArtistSorting>(ArtistSorting.Name(true))

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked collaborators
        every { viewModel.artists } returns artists
        every { viewModel.sorting } returns sorting
        every { viewModel.update } returns update
        every { viewModel.sort(any()) } returns Unit

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<ArtistsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showArtists_WhenResumed() {
        // Given
        val uiArtists = createUiArtists()

        mockkObject(ImageLoader)
        every { ImageLoader.loadImage(any(),any(),any(),any()) } returns Unit

        // When
        artists.value = uiArtists
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        uiArtists.forEachIndexed { index, artist ->
            onView(withId(R.id.artists))
                .perform(scrollToPosition<AlbumsAdapter.AlbumViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.artists)
                    .atPositionOnView(index, R.id.artist_name)
            )
                .check(matches(withText(artist.name)))

            scenario.onActivity {
                val fragment = it.supportFragmentManager.fragments[0]
                val imageView = fragment.requireView().findViewById<ImageView>(R.id.artist_art)!!
                verify {
                    ImageLoader.loadImage(
                        imageView.context,
                        artist.art,
                        R.drawable.ic_round_music_note_24,
                        imageView
                    )
                }
            }
        }
    }

    @Test
    fun showArtistsSorting_WhenOpeningSortingMenu() {
        // Given

        // When
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_artists_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_artist_name)),
                instanceOf(RelativeLayout::class.java)
            )
        )
            .check(matches(hasSibling(isChecked())))

        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_ascending)),
                instanceOf(RelativeLayout::class.java)
            )
        )
            .check(matches(hasSibling(isChecked())))

        // When
        sorting.value = ArtistSorting.Date(false)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_date_added)),
                instanceOf(RelativeLayout::class.java)
            )
        )
            .check(matches(hasSibling(isChecked())))

        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_descending)),
                instanceOf(RelativeLayout::class.java)
            )
        )
            .check(matches(hasSibling(isChecked())))
    }

    @Test
    fun showLoadingIndicator_WhenAlbumsLoading() {
        // Given

        // When
        update.value = ViewUpdateState.Loading
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))

        // When
        update.value = ViewUpdateState.EndLoading
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.progressBar))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun sortArtists_WhenSortingSelected() {
        // Given

        // When
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_artists_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_date_added)),
                instanceOf(RelativeLayout::class.java)
            )
        )
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_artists_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_descending)),
                instanceOf(RelativeLayout::class.java)
            ))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()


        // Then
        verify { viewModel.sort(ArtistSorting.Name(true)) }
        verify { viewModel.sort(ArtistSorting.Date(false)) }
    }
}
