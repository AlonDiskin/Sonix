package com.diskin.alon.sonix.catalog.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diskin.alon.sonix.catalog.application.model.AlbumDto
import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.catalog.application.model.AlbumsRequest
import com.diskin.alon.sonix.catalog.application.model.AlbumsResponse
import com.diskin.alon.sonix.catalog.application.usecase.GetAlbumsUseCase
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbum
import com.diskin.alon.sonix.catalog.presentation.util.ModelAlbumMapper
import com.diskin.alon.sonix.catalog.presentation.viewmodel.AlbumsViewModel
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [AlbumsViewModel] unit test.
 */
@RunWith(JUnitParamsRunner::class)
class AlbumsViewModelTest {

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
    private lateinit var viewModel: AlbumsViewModel

    // Collaborators
    private val getAlbums: GetAlbumsUseCase = mockk()
    private val mapper: ModelAlbumMapper = mockk()

    // Stub data
    private val albumsSubject = BehaviorSubject.create<AppResult<AlbumsResponse>>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { getAlbums.execute(any()) } returns albumsSubject

        viewModel = AlbumsViewModel(getAlbums, mapper)
    }

    @Test
    fun getLastSortedAlbumsFromModel_WhenCreated() {
        // Given

        // Then
        verify { getAlbums.execute(AlbumsRequest.LastSorted) }
    }

    @Test
    @Parameters(method = "sortingParams")
    fun getSortedAlbumsFromModel_WhenSorted(sorting: AlbumSorting) {
        // Given

        // When
        viewModel.sort(sorting)

        // Then
        verify { getAlbums.execute(AlbumsRequest.GetSorted(sorting)) }
    }

    @Test
    fun updateAlums_WhenModelAlbumsUpdated() {
        // Given
        val modelAlbums = mockk<List<AlbumDto>>()
        val sorting = mockk<AlbumSorting>()
        val mappedAlbums = mockk<List<UiAlbum>>()

        every { mapper.map(modelAlbums) } returns mappedAlbums

        // When
        albumsSubject.onNext(AppResult.Success(AlbumsResponse(modelAlbums,sorting)))

        // Then
        assertThat(viewModel.albums.value).isEqualTo(mappedAlbums)
    }

    @Test
    fun notifyViewAlbumsAreUpdated_WhenModelUpdatesAlbums() {
        // Given

        // When
        albumsSubject.onNext(AppResult.Loading())

        // Then
        assertThat(viewModel.update.value).isEqualTo(ViewUpdateState.Loading)
    }

    fun sortingParams() = arrayOf(
        AlbumSorting.Name(true),
        AlbumSorting.Name(false),
        AlbumSorting.Artist(true),
        AlbumSorting.Artist(false)
    )
}