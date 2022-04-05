package com.diskin.alon.sonix.catalog.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
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
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksAdapter.AudioTrackViewHolder
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTracksFragment
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AudioTracksViewModel
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.presentation.SingleLiveEvent
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.sonix.common.uitesting.launchFragmentInHiltContainer
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast

/**
 * [AudioTracksFragment] unit test class.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class AudioTracksFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel = mockk<AudioTracksViewModel>()
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val tracks = MutableLiveData<List<UiAudioTrack>>()
    private val update = MutableLiveData<ViewUpdateState>()
    private val sorting = MutableLiveData<AudioTracksSorting>(AudioTracksSorting.DateAdded(false))
    private val error = SingleLiveEvent<AppError>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked collaborators
        every { viewModel.tracks } returns tracks
        every { viewModel.sorting } returns sorting
        every { viewModel.update } returns update
        every { viewModel.sortTracks(any()) } returns Unit
        every { viewModel.error } returns error

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<AudioTracksFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Set test nav controller
        navController.setGraph(R.navigation.catalog_nav_graph)
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragments.first()
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun showDeviceTracks_WhenResumed() {
        // Given
        val uiTracks = createUiTracks()

        // When
        tracks.value = uiTracks
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        uiTracks.forEachIndexed { index, track ->
            onView(withId(R.id.tracks))
                .perform(RecyclerViewActions.scrollToPosition<AudioTrackViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.tracks).atPositionOnView(index, R.id.track_name))
                .check(matches(withText(track.name)))

            onView(withRecyclerView(R.id.tracks).atPositionOnView(index, R.id.track_artist))
                .check(matches(withText(track.artist)))
        }
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

    @Test
    fun showTrackCurrentSorting_WhenOpenSortingMenu() {
        // Given

        // When
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_tracks_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_date_added)),
                instanceOf(RelativeLayout::class.java)
            ))
            .check(matches(hasSibling(isChecked())))

        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_descending)),
                instanceOf(RelativeLayout::class.java)
            ))
            .check(matches(hasSibling(isChecked())))

        // When
        sorting.value = AudioTracksSorting.ArtistName(true)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_artist_name)),
                instanceOf(RelativeLayout::class.java)
            ))
            .check(matches(hasSibling(isChecked())))

        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_ascending)),
                instanceOf(RelativeLayout::class.java)
            ))
            .check(matches(hasSibling(isChecked())))
    }

    @Test
    fun showLoadingIndicator_WhenTracksLoaded() {
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
    fun sortTracks_WhenSortingSelectedByUser() {
        // Given

        // When
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_tracks_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_artist_name)),
                instanceOf(RelativeLayout::class.java)
            ))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.sortTracks(AudioTracksSorting.ArtistName(false)) }

        // When
        sorting.value = AudioTracksSorting.ArtistName(false)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
        onView(withText(R.string.title_sort_tracks_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(
            allOf(
                hasDescendant(withText(R.string.title_action_sort_ascending)),
                instanceOf(RelativeLayout::class.java)
            ))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.sortTracks(AudioTracksSorting.ArtistName(true)) }
    }

    @Test
    fun openTrackDetailScreen_WhenUserSelectToSeeTrackDetail() {
        // Given
        val uiTracks = createUiTracks()
        tracks.value = uiTracks

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
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
                    .getString(R.string.arg_track_id))
        )
            .isEqualTo(uiTracks.first().id)
    }

    @Test
    fun shareTrack_WhenUserSelectToShareTrack() {
        // Given
        val uiTracks = createUiTracks()
        tracks.value = uiTracks
        val firstTrackUri = Uri.parse(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            .toString().plus("/${uiTracks.first().id}"))

        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Intents.init()

        // When
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
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
    fun showConfirmationDialog_WhenUserSelectToDeleteTrack() {
        // Given
        val uiTracks = createUiTracks()
        tracks.value = uiTracks

        every { viewModel.deleteTrack(uiTracks.first().id) } returns Unit

        // When
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView((withText(R.string.title_action_delete_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val dialog = (ShadowAlertDialog.getLatestDialog() as AlertDialog)

        assertThat(dialog.isShowing).isTrue()
    }

    @Test
    fun deleteTrack_WhenUserSelectToDeleteTrackFromDialog() {
        // Given
        val uiTracks = createUiTracks()
        tracks.value = uiTracks

        every { viewModel.deleteTrack(uiTracks.first().id) } returns Unit

        // When
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView((withText(R.string.title_action_delete_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.deleteTrack(uiTracks.first().id) }
    }

    @Test
    fun doNotDeleteTrack_WhenUserCancelTrackDeletionFromDialog() {
        // Given
        val uiTracks = createUiTracks()
        tracks.value = uiTracks

        every { viewModel.deleteTrack(uiTracks.first().id) } returns Unit

        // When
        onView(withRecyclerView(R.id.tracks).atPositionOnView(0, R.id.track_menu))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView((withText(R.string.title_action_delete_track)))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        (ShadowAlertDialog.getLatestDialog() as AlertDialog)
            .getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify(exactly = 0) { viewModel.deleteTrack(uiTracks.first().id) }
    }

    @Test
    fun playAllTracks_WhenTrackSelected() {
        // Given
        val uiTracks = createUiTracks()
        tracks.value = uiTracks

        every { viewModel.playTracks(any(),any()) } returns Unit
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withRecyclerView(R.id.tracks).atPosition(0))
            .perform(click())

        // Then
        verify { viewModel.playTracks(0,uiTracks.map { it.id }) }
    }
}