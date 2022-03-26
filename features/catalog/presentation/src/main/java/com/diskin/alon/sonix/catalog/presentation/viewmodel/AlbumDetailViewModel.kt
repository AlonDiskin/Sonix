package com.diskin.alon.sonix.catalog.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.sonix.catalog.application.model.PlayTracksRequest
import com.diskin.alon.sonix.catalog.application.usecase.DeleteDeviceTrackUseCase
import com.diskin.alon.sonix.catalog.application.usecase.GetAlbumDetailUseCase
import com.diskin.alon.sonix.catalog.application.usecase.PlayTracksUseCase
import com.diskin.alon.sonix.catalog.presentation.model.UiAlbumDetail
import com.diskin.alon.sonix.catalog.presentation.util.ModelAlbumTracksMapper
import com.diskin.alon.sonix.catalog.presentation.util.ModelAlbumsMapper
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import com.diskin.alon.sonix.common.application.mapAppResult
import com.diskin.alon.sonix.common.presentation.RxViewModel
import com.diskin.alon.sonix.common.presentation.SingleLiveEvent
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val albumDetailUseCase: GetAlbumDetailUseCase,
    private val playTracksUseCase: PlayTracksUseCase,
    private val deleteTrackUseCase: DeleteDeviceTrackUseCase,
    private val albumsMapper: ModelAlbumsMapper,
    private val tracksMapper: ModelAlbumTracksMapper,
    private val stateHandle: SavedStateHandle
) : RxViewModel() {

    companion object { const val KEY_ALBUM_ID = "album_id" }

    private val _detail = MutableLiveData<UiAlbumDetail>()
    val detail: LiveData<UiAlbumDetail> get() = _detail
    private val _update = MutableLiveData<ViewUpdateState>()
    val update: LiveData<ViewUpdateState> get() = _update
    val error = SingleLiveEvent<AppError>()
    private val playTracksSubject = BehaviorSubject.create<PlayTracksRequest>()
    private val deletionSubject = BehaviorSubject.create<Int>()

    init {
        addSubscription(
            createAlbumDetailSubscription(),
            createTracksPlayingSubscription(),
            createTrackDeletionSubscription()
        )
    }

    fun playTracks(startIndex: Int, ids: List<Int>) {
        playTracksSubject.onNext(PlayTracksRequest(startIndex,ids))
    }

    fun deleteTrack(trackId: Int) {
        deletionSubject.onNext(trackId)
    }

    private fun createAlbumDetailSubscription(): Disposable {
        val albumId = stateHandle.get<Int>(KEY_ALBUM_ID) ?:
        throw IllegalStateException("Must contain album id arg in saved state!")

        return albumDetailUseCase.execute(albumId)
            .observeOn(AndroidSchedulers.mainThread())
            .mapAppResult {
                UiAlbumDetail(
                    albumsMapper.map(listOf(it.album)).first(),
                    tracksMapper.map(it.tracks)
                )
            }
            .subscribe(::handleAlbumDetailResult,::handleAlbumDetailSubscriptionError)
    }

    private fun handleAlbumDetailResult(result: AppResult<UiAlbumDetail>) {
        when(result) {
            is AppResult.Success -> {
                _update.value = ViewUpdateState.EndLoading
                _detail.value = result.data
            }
            is AppResult.Loading -> _update.value = ViewUpdateState.Loading
            is AppResult.Error -> error.value = result.error
        }
    }

    private fun handleAlbumDetailSubscriptionError(error: Throwable) {
        error.printStackTrace()
    }

    private fun createTracksPlayingSubscription(): Disposable {
        return playTracksSubject.switchMapSingle { playTracksUseCase.execute(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({},this::handleTracksPlayingSubscriptionError)
    }

    private fun handleTracksPlayingSubscriptionError(throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun createTrackDeletionSubscription(): Disposable {
        return deletionSubject
            .switchMapSingle(deleteTrackUseCase::execute)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleTrackDeletionResult,::handleTrackDeletionSubscriptionError)
    }

    private fun handleTrackDeletionResult(appResult: AppResult<Unit>) {
        if (appResult is AppResult.Error) {
            error.value = appResult.error
        }
    }

    private fun handleTrackDeletionSubscriptionError(throwable: Throwable) {
        throwable.printStackTrace()
        error.value = AppError.UNKNOWN_ERROR
    }
}