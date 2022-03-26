package com.diskin.alon.sonix.catalog.presentation

import android.os.Looper
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
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
import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumsAdapter
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumsFragment
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbum
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumDetailViewModel
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumsViewModel
import com.diskin.alon.sonix.common.presentation.ImageLoader
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.assertThat
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
 * [AlbumsFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class AlbumsFragmentTest {

    // System under test
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: AlbumsViewModel = mockk()
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val albums = MutableLiveData<List<UiAlbum>>()
    private val update = MutableLiveData<ViewUpdateState>()
    private val sorting = MutableLiveData<AlbumSorting>(AlbumSorting.Name(true))

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked collaborators
        every { viewModel.albums } returns albums
        every { viewModel.sorting } returns sorting
        every { viewModel.update } returns update
        every { viewModel.sort(any()) } returns Unit

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<AlbumsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set test nav controller
        navController.setGraph(R.navigation.catalog_nav_graph)
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragments.first()
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun showAlbums_WhenResumed() {
        // Given
        val uiAlbums = createUiAlbums()

        mockkObject(ImageLoader)
        every { ImageLoader.loadImage(any(),any(),any(),any()) } returns Unit

        // When
        albums.value = uiAlbums
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        uiAlbums.forEachIndexed { index, album ->
            onView(withId(R.id.albums))
                .perform(scrollToPosition<AlbumsAdapter.AlbumViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.albums)
                    .atPositionOnView(index, R.id.album_name)
            )
                .check(matches(withText(album.name)))

            onView(
                withRecyclerView(R.id.albums)
                    .atPositionOnView(index, R.id.album_artist)
            )
                .check(matches(withText(album.artist)))

            scenario.onActivity {
                val fragment = it.supportFragmentManager.fragments[0]
                val imageView = fragment.requireView().findViewById<ImageView>(R.id.album_art)!!
                verify {
                    ImageLoader.loadImage(
                        imageView.context,
                        album.art,
                        R.drawable.ic_round_music_note_24,
                        imageView
                    )
                }
            }
        }
    }

    @Test
    fun showAlbumsSorting_WhenOpeningSortingMenu() {
        // Given

        // When
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_albums_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_album_name)),
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
        sorting.value = AlbumSorting.Artist(false)
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
    fun sortAlbums_WhenSortingSelected() {
        // Given

        // When
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_albums_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_artist_name)),
                instanceOf(RelativeLayout::class.java)
            )
        )
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_albums_menu))
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
        verify { viewModel.sort(AlbumSorting.Artist(true)) }
        verify { viewModel.sort(AlbumSorting.Artist(false)) }
    }

    @Test
    fun openAlbumDetail_WhenAlbumSelected() {
        // Given
        val uiAlbums = createUiAlbums()

        albums.value = uiAlbums
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withRecyclerView(R.id.albums).atPosition(0))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(navController.currentDestination!!.id).isEqualTo(R.id.albumDetailFragment)
        assertThat(
            navController.currentBackStackEntry?.arguments?.get(AlbumDetailViewModel.KEY_ALBUM_ID)
        )
            .isEqualTo(uiAlbums.first().id)
    }
}
