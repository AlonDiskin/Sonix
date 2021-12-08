package com.diskin.alon.sonix.catalog.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diskin.alon.sonix.catalog.application.model.AudioTrackDto
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.model.PlayTracksRequest
import com.diskin.alon.sonix.catalog.application.usecase.DeleteDeviceTrackUseCase
import com.diskin.alon.sonix.catalog.application.usecase.GetLastTracksSortingUseCase
import com.diskin.alon.sonix.catalog.application.usecase.GetSortedDeviceTracksUseCase
import com.diskin.alon.sonix.catalog.application.usecase.PlayTracksUseCase
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack
import com.diskin.alon.sonix.catalog.presentation.util.ModelTracksMapper
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AudioTracksViewModel
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import io.reactivex.android.plugins.RxAndroidPlugins
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.SingleSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.*
import org.junit.runner.RunWith

/**
 * [AudioTracksViewModel] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
class AudioTracksViewModelTest {

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
    private lateinit var viewModel: AudioTracksViewModel

    // Collaborators
    private val getLastSorting: GetLastTracksSortingUseCase = mockk()
    private val getSortedTracks: GetSortedDeviceTracksUseCase = mockk()
    private val deleteDeviceTrackUseCase: DeleteDeviceTrackUseCase = mockk()
    private val playTracksUseCase: PlayTracksUseCase = mockk()
    private val tracksMapper: ModelTracksMapper = mockk()

    // Stub data
    private val sortingSubject = SingleSubject.create<AppResult<AudioTracksSorting>>()
    private val tracksSubject = BehaviorSubject.create<AppResult<List<AudioTrackDto>>>()
    private val deletionSubject = SingleSubject.create<AppResult<Unit>>()

    @Before
    fun setUp() {
        // Stub mocks
        every { getSortedTracks.execute(any()) } returns tracksSubject
        every { getLastSorting.execute(Unit) } returns sortingSubject
        every { deleteDeviceTrackUseCase.execute(any()) } returns deletionSubject

        viewModel = AudioTracksViewModel(
            getLastSorting,
            getSortedTracks,
            deleteDeviceTrackUseCase,
            playTracksUseCase,
            tracksMapper
        )
    }

    @Test
    fun loadLastSorted_WhenCreated() {
        // Given
        val lastSorting = mockk<AudioTracksSorting>()

        // When
        sortingSubject.onSuccess(AppResult.Success(lastSorting))

        // Then
        verify { getLastSorting.execute(Unit) }
        verify { getSortedTracks.execute(lastSorting) }
    }

    @Test
    fun notifyViewUpdateState_WhenLoadedFromModel() {
        // Given

        // When
        sortingSubject.onSuccess(AppResult.Success(AudioTracksSorting.DateAdded(false)))
        tracksSubject.onNext(AppResult.Loading())

        // Then
        assertThat(viewModel.update.value).isEqualTo(ViewUpdateState.Loading)
    }

    @Test
    fun updateViewTracks_WhenLoadedFromModel() {
        // Given
        val repoTracks = mockk<List<AudioTrackDto>>()
        val mappedTracks = mockk<List<UiAudioTrack>>()

        every { tracksMapper.map(repoTracks) } returns mappedTracks

        // When
        sortingSubject.onSuccess(AppResult.Success(AudioTracksSorting.DateAdded(false)))
        tracksSubject.onNext(AppResult.Success(repoTracks))

        // Then
        assertThat(viewModel.tracks.value).isEqualTo(mappedTracks)
    }

    @Test
    @Parameters(method = "sortingParams")
    fun updateViewTracksSorting_WhenTracksModelLoaded(sorting: AudioTracksSorting) {
        // Given
        val repoTracks = mockk<List<AudioTrackDto>>()
        val mappedTracks = mockk<List<UiAudioTrack>>()

        every { tracksMapper.map(repoTracks) } returns mappedTracks

        // When
        viewModel.sortTracks(sorting)
        tracksSubject.onNext(AppResult.Success(repoTracks))

        // Then
        assertThat(viewModel.sorting.value).isEqualTo(sorting)
    }

    @Test
    fun updateViewError_WhenModelTracksLoadFail() {
        // Given
        val modelError = mockk<AppError>()

        // When
        sortingSubject.onSuccess(AppResult.Success(AudioTracksSorting.DateAdded(false)))
        tracksSubject.onNext(AppResult.Error(modelError))

        // Then
        assertThat(viewModel.error.value).isEqualTo(modelError)
    }

    @Test
    @Parameters(method = "sortingParams")
    fun loadSortedTracksFromModel_WhenViewTracksSorted(sorting: AudioTracksSorting) {
        // Given

        // When
        viewModel.sortTracks(sorting)

        //Then
        verify { getSortedTracks.execute(sorting) }
    }

    @Test
    fun deleteTrackFromModel_WhenTrackDeleted() {
        // Given
        val trackId = 1

        // When
        viewModel.deleteTrack(trackId)

        // Then
        verify { deleteDeviceTrackUseCase.execute(trackId) }
    }

    @Test
    fun updateViewError_WhenModelTrackDeleteFail() {
        // Given
        val trackId = 1
        val modelError = mockk<AppError>()

        // When
        viewModel.deleteTrack(trackId)
        deletionSubject.onSuccess(AppResult.Error(modelError))

        // Then
        assertThat(viewModel.error.value).isEqualTo(modelError)
    }

    @Test
    fun playModelTracks_WhenRequested() {
        // Given
        val index = 1
        val ids = mockk<List<Int>>()

        every { playTracksUseCase.execute(any()) } returns Single.just(AppResult.Success(Unit))

        // When
        viewModel.playTracks(index,ids)

        // Then
        verify { playTracksUseCase.execute(PlayTracksRequest(index,ids)) }
    }

    private fun sortingParams() = arrayOf(
        AudioTracksSorting.DateAdded(true),
        AudioTracksSorting.DateAdded(false),
        AudioTracksSorting.ArtistName(true),
        AudioTracksSorting.ArtistName(false)
    )
}