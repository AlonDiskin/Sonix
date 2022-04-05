package com.diskin.alon.sonix.catalog.featuretesting.scenario.album_browser

import android.content.ContentResolver
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumDetailFragment
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumTracksAdapter
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumsFragment
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumDetailViewModel
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import io.mockk.every
import org.hamcrest.CoreMatchers.allOf
import org.robolectric.Shadows

class DetailShownSteps(
    private val contentResolver: ContentResolver
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val navController = TestNavHostController(getApplicationContext())
    private val albums = createDeviceAlbums()
    private val albumTracks = createDeviceAlbumTracks()

    init {
        // Stub mock contentResolver
        val albumsMediaContentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Albums.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        }
        val audioMediaContentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val albumColumns = arrayOf(
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )
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
        val albumDetailSelection = "${MediaStore.Audio.Albums.ALBUM_ID} = ?"
        val albumDetailSelectionArgs = arrayOf(albums.first().id.toString())
        val albumsCursor = createAlbumMediaStoreCursor(albums)
        val albumDetailCursor = createAlbumMediaStoreCursor(listOf(albums.first()))
        val albumTracksCursor = createAudioMediaStoreCursor(albumTracks)
        val albumTracksSelection = "${MediaStore.Audio.Media.ALBUM_ID} = ?"
        val albumTracksSelectionArgs = arrayOf(albums.first().id.toString())

        every { contentResolver.query(albumsMediaContentUri,albumColumns,null,null,any()) } returns albumsCursor
        every { contentResolver.query(albumsMediaContentUri,albumColumns,albumDetailSelection,albumDetailSelectionArgs,any()) } returns albumDetailCursor
        every { contentResolver.query(audioMediaContentUri,audioColumns,albumTracksSelection,albumTracksSelectionArgs,any()) } returns albumTracksCursor
        every { contentResolver.registerContentObserver(albumsMediaContentUri,any(),any()) } returns Unit
        every { contentResolver.registerContentObserver(audioMediaContentUri,any(),any()) } returns Unit
        every { contentResolver.unregisterContentObserver(any()) } returns Unit

        // Setup nav controller
        navController.setGraph(R.navigation.catalog_nav_graph)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.albumDetailFragment) {
                val keyAlbumId = AlbumDetailViewModel.KEY_ALBUM_ID
                val albumIdArg = navController.currentBackStackEntry!!.arguments!!.getLong(keyAlbumId)
                val bundle = bundleOf(AlbumDetailViewModel.KEY_ALBUM_ID to albumIdArg)
                scenario = launchFragmentInHiltContainer<AlbumDetailFragment>(fragmentArgs = bundle)
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
        }
    }

    @Given("^user selected an album from browser listing$")
    fun user_selected_an_album_from_browser_listing() {
        scenario = launchFragmentInHiltContainer<AlbumsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragments.first()
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withRecyclerView(R.id.albums).atPosition(0))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^album detail should be shown$")
    fun album_detail_should_be_shown() {
        onView(withId(R.id.album_name))
            .check(matches(allOf(withText(albums.first().name), isDisplayed())))
        onView(withId(R.id.album_artist))
            .check(matches(allOf(withText(albums.first().artist), isDisplayed())))
        onView(withId(R.id.album_tracks))
            .check(matches(isRecyclerViewItemsCount(albumTracks.size)))

        albumTracks.forEachIndexed { index, track ->
            onView(withId(R.id.album_tracks))
                .perform(scrollToPosition<AlbumTracksAdapter.AlbumTrackViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.album_tracks)
                    .atPositionOnView(index, R.id.track_name)
            )
                .check(matches(withText(track.name)))
        }
    }
}