package com.diskin.alon.sonix.catalog.presentation

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDetailDto
import com.diskin.alon.sonix.catalog.application.usecase.GetDeviceTrackDetailUseCase
import com.diskin.alon.sonix.catalog.presentation.util.ModelTrackDetailMapper
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AudioTrackDetailViewModel
import com.diskin.alon.sonix.common.application.AppResult
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * [AudioTrackDetailViewModel] unit test class.
 */
class AudioTrackDetailViewModelTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Lifecycle testing rule
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test subject
    private lateinit var viewModel: AudioTrackDetailViewModel

    // Collaborators
    private val getDeviceTrackDetailUseCase: GetDeviceTrackDetailUseCase = mockk()
    private val trackDetailMapper: ModelTrackDetailMapper = mockk()
    private val savedState: SavedStateHandle = SavedStateHandle()
    private val resources: Resources = mockk()

    // Stub data
    private val trackDetailSubject = BehaviorSubject.create<AppResult<AudioTrackDetailDto>>()
    private val idKey = "track id"
    private val trackId = 1L

    @Before
    fun setUp() {
        // Stub collaborators
        every { resources.getString(R.string.arg_track_id) } returns idKey
        every { getDeviceTrackDetailUseCase.execute(trackId) } returns trackDetailSubject

        savedState.set(idKey,trackId)

        viewModel = AudioTrackDetailViewModel(
            getDeviceTrackDetailUseCase,
            trackDetailMapper,
            savedState,
            resources
        )
    }

    @Test(expected = IllegalStateException::class)
    fun throwException_WhenCreatedWithoutTrackIdState() {
        // Given
        val emptySavedState = SavedStateHandle()

        // When
        viewModel = AudioTrackDetailViewModel(
            getDeviceTrackDetailUseCase,
            trackDetailMapper,
            emptySavedState,
            resources
        )

        // Then
    }

    @Test
    fun observeTrackDetailFromModel_WhenCreated() {
        // Given
        val modelTrackDetail = mockk<AudioTrackDetailDto>()
        val trackUiDetail = createUiTrackDetail()

        every { trackDetailMapper.map(modelTrackDetail) } returns trackUiDetail

        // Then
        verify { getDeviceTrackDetailUseCase.execute(trackId) }

        // When
        trackDetailSubject.onNext(AppResult.Success(modelTrackDetail))

        // Then
        verify { trackDetailMapper.map(modelTrackDetail) }
        assertThat(viewModel.trackDetail.value).isEqualTo(trackUiDetail)
    }
}