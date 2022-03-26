package com.diskin.alon.sonix.catalog.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.sonix.catalog.application.model.AlbumDetailResponse
import com.diskin.alon.sonix.catalog.application.model.PlayTracksRequest
import com.diskin.alon.sonix.catalog.application.usecase.DeleteDeviceTrackUseCase
import com.diskin.alon.sonix.catalog.application.usecase.GetAlbumDetailUseCase
import com.diskin.alon.sonix.catalog.application.usecase.PlayTracksUseCase
import com.diskin.alon.sonix.catalog.presentation.util.ModelAlbumTracksMapper
import com.diskin.alon.sonix.catalog.presentation.util.ModelAlbumsMapper
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumDetailViewModel
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * [AlbumDetailViewModel] unit test class.
 */
class AlbumDetailViewModelTest {

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
    private lateinit var viewModel: AlbumDetailViewModel

    // Collaborators
    private val albumDetailUseCase: GetAlbumDetailUseCase = mockk()
    private val playTracksUseCase: PlayTracksUseCase = mockk()
    private val deleteTrackUseCase: DeleteDeviceTrackUseCase = mockk()
    private val albumsMapper: ModelAlbumsMapper = mockk()
    private val tracksMapper: ModelAlbumTracksMapper = mockk()
    private val stateHandle: SavedStateHandle  = SavedStateHandle()

    // Stub data
    private val albumDetailSubject = BehaviorSubject.create<AppResult<AlbumDetailResponse>>()
    private val albumId = 1

    @Before
    fun setUp() {
        // Stub mocked collaborators
        every { albumDetailUseCase.execute(any()) } returns albumDetailSubject
        stateHandle.set(AlbumDetailViewModel.KEY_ALBUM_ID,albumId)

        // Init subject
        viewModel = AlbumDetailViewModel(
            albumDetailUseCase,
            playTracksUseCase,
            deleteTrackUseCase,
            albumsMapper,
            tracksMapper,
            stateHandle
        )
    }

    @Test
    fun getAlbumDetailFromModel_WhenCreated() {
        // Given

        // Then
        verify { albumDetailUseCase.execute(albumId) }
    }

    @Test(expected = IllegalStateException::class)
    fun throwException_WhenCreatedWithoutAlbumIdState() {
        // Given
        val emptySavedState = SavedStateHandle()

        // When
        viewModel = AlbumDetailViewModel(
            albumDetailUseCase,
            playTracksUseCase,
            deleteTrackUseCase,
            albumsMapper,
            tracksMapper,
            emptySavedState
        )

        // Then
    }

    @Test
    fun notifyViewUpdateState_WhenDetailLoadedFromModel() {
        // Given

        // When
        albumDetailSubject.onNext(AppResult.Loading())

        // Then
        assertThat(viewModel.update.value).isEqualTo(ViewUpdateState.Loading)
    }

    @Test
    fun deleteTrackFromModel_WhenTrackDeleted() {
        // Given
        val trackId = 1

        // When
        viewModel.deleteTrack(trackId)

        // Then
        verify { deleteTrackUseCase.execute(trackId) }
    }

    @Test
    fun updateViewError_WhenAlbumDetailLoadFail() {
        // Given
        val modelError = mockk<AppError>()

        // When
        albumDetailSubject.onNext(AppResult.Error(modelError))

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
}