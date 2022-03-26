package com.diskin.alon.sonix.catalog.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumDetailFragment
import com.diskin.alon.sonix.catalog.presentation.controller.AlbumTracksAdapter
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbumDetail
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumDetailViewModel
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.presentation.ImageLoader
import com.diskin.alon.sonix.common.presentation.SingleLiveEvent
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class AlbumDetailFragmentTest {

    // System under test
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel = mockk<AlbumDetailViewModel>()
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val albumDetail = MutableLiveData<UiAlbumDetail>()
    private val update = MutableLiveData<ViewUpdateState>()
    private val error = SingleLiveEvent<AppError>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked collaborators
        every { viewModel.detail } returns albumDetail
        every { viewModel.update } returns update
        every { viewModel.error } returns error

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<AlbumDetailFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set test nav controller
        navController.setGraph(R.navigation.catalog_nav_graph)
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragments.first()
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun showAlbumDetail_WhenResumed() {
        // Given
        val detail = createUiAlbumDetail()

        mockkObject(ImageLoader)
        every { ImageLoader.loadImage(any(),any(),any(),any()) } returns Unit

        // When
        albumDetail.value = detail
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.album_name))
            .check(matches(allOf(withText(detail.album.name), isDisplayed())))
        onView(withId(R.id.album_artist))
            .check(matches(allOf(withText(detail.album.artist), isDisplayed())))
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]
            val imageView = fragment.requireView().findViewById<ImageView>(R.id.album_art)!!

            verify {
                ImageLoader.loadImage(
                    imageView.context,
                    detail.album.art,
                    R.drawable.ic_round_music_note_24,
                    imageView
                )
            }
        }
        detail.tracks.forEachIndexed { index, track ->
            onView(withId(R.id.album_tracks))
                .perform(RecyclerViewActions.scrollToPosition<AlbumTracksAdapter.AlbumTrackViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.album_tracks)
                    .atPositionOnView(index, R.id.track_name)
            )
                .check(matches(withText(track.name)))
        }
    }

    @Test
    fun showLoadingIndicator_WhenDetailLoading() {
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
    fun openTrackDetailScreen_WhenUserSelectToSeeTrackDetail() {
        // Given
        val uiDetail = createUiAlbumDetail()
        albumDetail.value = uiDetail

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withRecyclerView(R.id.album_tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView((withText(R.string.title_action_track_detail)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertThat(navController.currentDestination!!.id).isEqualTo(R.id.audioTrackDetailDialog)
        assertThat(
            navController.currentBackStackEntry?.arguments?.get(
                ApplicationProvider.getApplicationContext<Context>()
                    .getString(R.string.arg_track_id)
            )
        )
            .isEqualTo(uiDetail.tracks.first().id)
    }

    @Test
    fun shareTrack_WhenUserSelectToShareTrack() {
        // Given
        val uiDetail = createUiAlbumDetail()
        albumDetail.value = uiDetail
        val firstTrackUri = Uri.parse(
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            .toString().plus("/${uiDetail.tracks.first().id}"))

        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Intents.init()

        // When
        onView(withRecyclerView(R.id.album_tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView((withText(R.string.title_action_share_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        Intents.intended(IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
        Intents.intended(IntentMatchers.hasExtraWithKey(Intent.EXTRA_INTENT))

        val intent = Intents.getIntents().first().extras?.get(Intent.EXTRA_INTENT) as Intent

        assertThat(intent.type).isEqualTo(ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.mime_type_audio))
        assertThat(intent.clipData!!.getItemAt(0).uri).isEqualTo(firstTrackUri)

        Intents.release()
    }

    @Test
    fun playAllTracks_WhenTrackSelected() {
        // Given
        val uiDetail = createUiAlbumDetail()
        albumDetail.value = uiDetail

        every { viewModel.playTracks(any(),any()) } returns Unit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withRecyclerView(R.id.album_tracks).atPosition(0))
            .perform(click())

        // Then
        verify { viewModel.playTracks(0,uiDetail.tracks.map { it.id }) }
    }

    fun deleteTrack_WhenUserSelectToDeleteTrackFromDialog() {
        // Given
        val uiDetail = createUiAlbumDetail()
        albumDetail.value = uiDetail

        every { viewModel.deleteTrack(uiDetail.tracks.first().id) } returns Unit

        // When
        onView(withRecyclerView(R.id.album_tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView((withText(R.string.title_action_delete_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.deleteTrack(uiDetail.tracks.first().id) }
    }

    @Test
    fun doNotDeleteTrack_WhenUserCancelTrackDeletionFromDialog() {
        // Given
        val uiDetail = createUiAlbumDetail()
        albumDetail.value = uiDetail

        every { viewModel.deleteTrack(uiDetail.tracks.first().id) } returns Unit

        // When
        onView(withRecyclerView(R.id.album_tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView((withText(R.string.title_action_delete_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 0) { viewModel.deleteTrack(uiDetail.tracks.first().id) }
    }

    @Test
    fun showErrorNotification_UponDeviceStorageError() {
        // Given
        val appError = AppError.DEVICE_STORAGE

        // When
        error.value = appError
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.error_message_storage)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }

    @Test
    fun showErrorNotification_UponUnknownError() {
        // Given
        val appError = AppError.UNKNOWN_ERROR

        // When
        error.value = appError
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.error_message_unknown)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }
}