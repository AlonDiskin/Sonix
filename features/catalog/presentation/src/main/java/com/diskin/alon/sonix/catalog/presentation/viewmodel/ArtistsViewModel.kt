package com.diskin.alon.sonix.catalog.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.catalog.application.model.ArtistsRequest
import com.diskin.alon.sonix.catalog.application.usecase.GetArtistsUseCase
import com.diskin.alon.sonix.catalog.presentation.model.UiArtist
import com.diskin.alon.sonix.catalog.presentation.util.ModelArtistsMapper
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.mapAppResult
import com.diskin.alon.sonix.common.presentation.RxViewModel
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    private val getArtists: GetArtistsUseCase,
    private val mapper: ModelArtistsMapper
) : RxViewModel() {

    private val _albums = MutableLiveData<List<UiArtist>>()
    val artists: LiveData<List<UiArtist>> get() = _albums
    private val _update = MutableLiveData<ViewUpdateState>()
    val update: LiveData<ViewUpdateState> get() = _update
    private val _sorting = MutableLiveData<ArtistSorting>()
    val sorting: LiveData<ArtistSorting> get() = _sorting
    private val albumsRequestSubject = BehaviorSubject.createDefault<ArtistsRequest>(ArtistsRequest.LastSorted)

    init {
        addSubscription(
            createArtistsRequestSubscription()
        )
    }

    fun sort(sorting: ArtistSorting) {
        albumsRequestSubject.onNext(ArtistsRequest.GetSorted(sorting))
    }

    private fun createArtistsRequestSubscription(): Disposable {
        return albumsRequestSubject.switchMap(getArtists::execute)
            .observeOn(AndroidSchedulers.mainThread())
            .mapAppResult{ Pair(mapper.map(it.artists),it.sorting) }
            .subscribe(::handleArtistsResult, ::handleArtistsRequestSubscriptionError)
    }

    private fun handleArtistsResult(appResult: AppResult<Pair<List<UiArtist>, ArtistSorting>>) {
        when(appResult) {
            is AppResult.Success -> {
                _update.value = ViewUpdateState.EndLoading
                _albums.value = appResult.data.first!!
                _sorting.value = appResult.data.second!!
            }

            is AppResult.Loading -> _update.value = ViewUpdateState.Loading

            is AppResult.Error -> {
                _update.value = ViewUpdateState.EndLoading
                // TODO update view error
            }
        }
    }

    private fun handleArtistsRequestSubscriptionError(throwable: Throwable) {
        throwable.printStackTrace()
    }
}