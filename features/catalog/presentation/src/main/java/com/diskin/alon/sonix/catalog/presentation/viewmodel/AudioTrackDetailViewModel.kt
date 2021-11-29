package com.diskin.alon.sonix.catalog.presentation.viewmodel

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.diskin.alon.sonix.catalog.application.usecase.GetDeviceTrackDetailUseCase
import com.diskin.alon.sonix.catalog.application.util.AppError
import com.diskin.alon.sonix.catalog.application.util.AppResult
import com.diskin.alon.sonix.catalog.application.util.mapAppResult
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.model.UiAudioTrackDetail
import com.diskin.alon.sonix.catalog.presentation.util.ModelTrackDetailMapper
import com.diskin.alon.sonix.common.presentation.RxViewModel
import com.diskin.alon.sonix.common.presentation.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class AudioTrackDetailViewModel @Inject constructor(
    private val getDeviceTrackDetailUseCase: GetDeviceTrackDetailUseCase,
    private val trackDetailMapper: ModelTrackDetailMapper,
    savedState: SavedStateHandle,
    resources: Resources
) : RxViewModel() {

    private val _trackDetail = MutableLiveData<UiAudioTrackDetail>()
    val trackDetail: LiveData<UiAudioTrackDetail> get() = _trackDetail
    val error = SingleLiveEvent<AppError>()

    init {
        val trackId = savedState.get<Int>(resources.getString(R.string.arg_track_id)) ?:
        throw IllegalStateException("Must have track id state!")

        addSubscription(createTrackDetailSubscription(trackId))
    }

    private fun createTrackDetailSubscription(trackId: Int): Disposable {
        return getDeviceTrackDetailUseCase.execute(trackId)
            .observeOn(AndroidSchedulers.mainThread())
            .mapAppResult(trackDetailMapper::map)
            .subscribe(::handleTrackDetailModelResult, ::handleTrackDetailSubscriptionError)
    }

    private fun handleTrackDetailModelResult(appResult: AppResult<UiAudioTrackDetail>) {
        when(appResult) {
            is AppResult.Success -> _trackDetail.value = appResult.data
            is AppResult.Error -> error.value = appResult.error
        }
    }

    private fun handleTrackDetailSubscriptionError(throwable: Throwable) {
        throwable.printStackTrace()
        error.value = AppError.UNKNOWN_ERROR
    }
}