package com.diskin.alon.sonix.catalog.presentation

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.sonix.catalog.presentation.controller.AudioTrackDetailDialog
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrackDetail
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AudioTrackDetailViewModel
import com.diskin.alon.sonix.common.presentation.SingleLiveEvent
import com.diskin.alon.sonix.common.uitesting.HiltTestActivity
import com.diskin.alon.sonix.common.uitesting.launchDialogInHiltContainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [AudioTrackDetailDialog] unit test class.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(instrumentedPackages = ["androidx.loader.content"])
class AudioTrackDetailDialogTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel = mockk<AudioTrackDetailViewModel>()

    // Stub data
    private val trackDetail = MutableLiveData<UiAudioTrackDetail>()
    private val error = SingleLiveEvent<com.diskin.alon.sonix.common.application.AppError>()

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked collaborators
        every { viewModel.trackDetail } returns trackDetail
        every { viewModel.error } returns error

        // Launch dialog under test
        scenario = launchDialogInHiltContainer<AudioTrackDetailDialog> { AudioTrackDetailDialog() }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showTrackDetail_WhenLoaded() {
        // Given
        val track = createUiTrackDetail()

        // When
        trackDetail.value = track
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.track_artist))
            .inRoot(isDialog())
            .check(matches(withText(track.artist)))
        onView(withId(R.id.track_album))
            .inRoot(isDialog())
            .check(matches(withText(track.album)))
        onView(withId(R.id.track_path))
            .inRoot(isDialog())
            .check(matches(withText(track.path)))
        onView(withId(R.id.track_size))
            .inRoot(isDialog())
            .check(matches(withText(track.size)))
        onView(withId(R.id.track_duration))
            .inRoot(isDialog())
            .check(matches(withText(track.duration)))
        onView(withId(R.id.track_format))
            .inRoot(isDialog())
            .check(matches(withText(track.format)))
    }
}