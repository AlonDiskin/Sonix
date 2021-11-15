package com.diskin.alon.sonix.catalog.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.catalog.application.usecase.GetLastTracksSortingUseCase
import com.diskin.alon.sonix.catalog.application.usecase.GetSortedTracksUseCase
import com.diskin.alon.sonix.catalog.application.util.AppError
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.application.util.mapAppResult
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrack
import com.diskin.alon.sonix.catalog.presentation.util.ModelTracksMapper
import com.diskin.alon.sonix.common.presentation.RxViewModel
import com.diskin.alon.sonix.common.presentation.SingleLiveEvent
import com.diskin.alon.sonix.common.presentation.ViewUpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class AudioTracksViewModel @Inject constructor(
    private val getLastSorting: GetLastTracksSortingUseCase,
    private val getSortedTracks: GetSortedTracksUseCase,
    private val tracksMapper: ModelTracksMapper
) : RxViewModel() {

    private val selectedSorting = BehaviorSubject.create<AudioTracksSorting>()
    private val _sorting = MutableLiveData<AudioTracksSorting>()
    val sorting: LiveData<AudioTracksSorting> get() = _sorting
    private val _tracks = MutableLiveData<List<UiAudioTrack>>()
    val tracks: LiveData<List<UiAudioTrack>> get() = _tracks
    private val _update = MutableLiveData<ViewUpdateState>()
    val update: LiveData<ViewUpdateState> get() = _update
    val error = SingleLiveEvent<AppError>()

    init {
        addSubscription(
            getLastSorting(),
            createModelTracksSubscription()
        )
    }

    private fun getLastSorting(): Disposable {
        return getLastSorting.execute(Unit)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when(it) {
                    is AppResult.Success -> selectedSorting.onNext(it.data)
                    is AppResult.Error -> error.value = it.error
                }
            },{
                it.printStackTrace()
                error.value = AppError.UNKNOWN_ERROR
            })
    }

    fun sortTracks(sorting: AudioTracksSorting) {
        selectedSorting.onNext(sorting)
    }

    private fun createModelTracksSubscription(): Disposable {
        return selectedSorting.switchMap(getSortedTracks::execute)
            .observeOn(AndroidSchedulers.mainThread())
            .mapAppResult(tracksMapper::map)
            .subscribe(::handleTracksResultUpdate,::handleTracksSubscriptionError)
    }

    private fun handleTracksResultUpdate(result: AppResult<List<UiAudioTrack>>) {
        when(result) {
            is AppResult.Success -> {
                _update.value = ViewUpdateState.EndLoading
                _tracks.value = result.data
                _sorting.value = selectedSorting.value
            }
            is AppResult.Error -> {
                _update.value = ViewUpdateState.EndLoading
                error.value = result.error
            }
            is AppResult.Loading -> _update.value = ViewUpdateState.Loading
        }
    }

    private fun handleTracksSubscriptionError(throwable: Throwable) {
        throwable.printStackTrace()
        error.value = AppError.UNKNOWN_ERROR
    }
}