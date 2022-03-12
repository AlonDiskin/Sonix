package com.diskin.alon.sonix.catalog.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.catalog.application.model.AlbumsRequest
import com.diskin.alon.sonix.catalog.application.usecase.GetAlbumsUseCase
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbum
import com.diskin.alon.sonix.catalog.presentation.util.ModelAlbumMapper
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
class AlbumsViewModel @Inject constructor(
    private val getAlbums: GetAlbumsUseCase,
    private val mapper: ModelAlbumMapper
) : RxViewModel() {

    private val _albums = MutableLiveData<List<UiAlbum>>()
    val albums: LiveData<List<UiAlbum>> get() = _albums
    private val _update = MutableLiveData<ViewUpdateState>()
    val update: LiveData<ViewUpdateState> get() = _update
    private val _sorting = MutableLiveData<AlbumSorting>()
    val sorting: LiveData<AlbumSorting> get() = _sorting
    private val albumsRequestSubject = BehaviorSubject.createDefault<AlbumsRequest>(AlbumsRequest.LastSorted)

    init {
        addSubscription(
            createAlbumsRequestSubscription()
        )
    }

    fun sort(sorting: AlbumSorting) {
        albumsRequestSubject.onNext(AlbumsRequest.GetSorted(sorting))
    }

    private fun createAlbumsRequestSubscription(): Disposable {
        return albumsRequestSubject.switchMap(getAlbums::execute)
            .observeOn(AndroidSchedulers.mainThread())
            .mapAppResult{ Pair(mapper.map(it.albums),it.sorting) }
            .subscribe(::handleAlbumsResult, ::handleAlbumsRequestSubscriptionError)
    }

    private fun handleAlbumsResult(appResult: AppResult<Pair<List<UiAlbum>, AlbumSorting>>) {
        when(appResult) {
            is AppResult.Success -> {
                _update.value = ViewUpdateState.EndLoading
                _albums.value = appResult.data.first!!
                _sorting.value = appResult.data.second!!
            }

            is AppResult.Loading -> _update.value = ViewUpdateState.Loading
        }
    }

    private fun handleAlbumsRequestSubscriptionError(throwable: Throwable) {
        throwable.printStackTrace()
    }
}