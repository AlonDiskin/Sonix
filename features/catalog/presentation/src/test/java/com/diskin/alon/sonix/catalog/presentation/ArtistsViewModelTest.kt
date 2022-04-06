package com.diskin.alon.sonix.catalog.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diskin.alon.sonix.catalog.application.model.ArtistDto
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.catalog.application.model.ArtistsRequest
import com.diskin.alon.sonix.catalog.application.model.ArtistsResponse
import com.diskin.alon.sonix.catalog.application.usecase.GetArtistsUseCase
import com.diskin.alon.sonix.catalog.presentation.model.UiArtist
import com.diskin.alon.sonix.catalog.presentation.util.ModelArtistsMapper
import com.diskin.alon.sonix.catalog.presentation.viewmodel.ArtistsViewModel
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
 * [ArtistsViewModel] unit test.
 */
@RunWith(JUnitParamsRunner::class)
class ArtistsViewModelTest {

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
    private lateinit var viewModel: ArtistsViewModel

    // Collaborators
    private val getArtists: GetArtistsUseCase = mockk()
    private val mapper: ModelArtistsMapper = mockk()

    // Stub data
    private val artistsSubject = BehaviorSubject.create<AppResult<ArtistsResponse>>()

    @Before
    fun setUp() {
        // Stub collaborators
        every { getArtists.execute(any()) } returns artistsSubject

        viewModel = ArtistsViewModel(getArtists, mapper)
    }

    @Test
    fun getLastSortedArtistsFromModel_WhenCreated() {
        // Given

        // Then
        verify { getArtists.execute(ArtistsRequest.LastSorted) }
    }

    @Test
    @Parameters(method = "sortingParams")
    fun getSortedArtistsFromModel_WhenSorted(sorting: ArtistSorting) {
        // Given

        // When
        viewModel.sort(sorting)

        // Then
        verify { getArtists.execute(ArtistsRequest.GetSorted(sorting)) }
    }

    @Test
    fun updateArtists_WhenModelArtistsUpdated() {
        // Given
        val modelArtists = mockk<List<ArtistDto>>()
        val sorting = mockk<ArtistSorting>()
        val mappedArtists = mockk<List<UiArtist>>()

        every { mapper.map(modelArtists) } returns mappedArtists

        // When
        artistsSubject.onNext(AppResult.Success(ArtistsResponse(modelArtists,sorting)))

        // Then
        assertThat(viewModel.artists.value).isEqualTo(mappedArtists)
    }

    @Test
    fun notifyViewAlbumsAreUpdated_WhenModelUpdatesAlbums() {
        // Given

        // When
        artistsSubject.onNext(AppResult.Loading())

        // Then
        assertThat(viewModel.update.value).isEqualTo(ViewUpdateState.Loading)
    }

    fun sortingParams() = arrayOf(
        ArtistSorting.Name(true),
        ArtistSorting.Name(false),
        ArtistSorting.Name(true),
        ArtistSorting.Name(false)
    )
}