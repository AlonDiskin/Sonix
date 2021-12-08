package com.diskin.alon.sonix.catalog.data

import android.content.SharedPreferences
import com.diskin.alon.sonix.catalog.application.interfaces.SortingStore
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Stores [AudioTracksSorting] to local storage and provide retrieval of last stored value.
 */
class SortingStoreImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SortingStore {

    companion object {
        private const val KEY_SORTING = "sorting"
        private const val KEY_ORDER = "order"
        private const val DEF_SORTING: String = "date"
        private const val DEF_ORDER = false
    }

    override fun getLast(): Single<AppResult<AudioTracksSorting>> {
        return Single.create<AppResult<AudioTracksSorting>> { emitter ->
            val lastSorting = when (sharedPreferences.getString(KEY_SORTING, DEF_SORTING)) {
                "date" ->
                    AudioTracksSorting.DateAdded(sharedPreferences.getBoolean(KEY_ORDER, DEF_ORDER))
                else ->
                    AudioTracksSorting.ArtistName(sharedPreferences.getBoolean(KEY_ORDER, DEF_ORDER))
            }

            emitter.onSuccess(AppResult.Success(lastSorting))
        }
            .onErrorReturn { AppResult.Error(com.diskin.alon.sonix.common.application.AppError.DEVICE_STORAGE) }
            .subscribeOn(Schedulers.io())
    }

    override fun save(sorting: AudioTracksSorting): Single<AppResult<Unit>> {
        return Single.create<AppResult<Unit>> { emitter ->
            sharedPreferences.edit()
                .putString(
                    KEY_SORTING,
                    when(sorting){
                        is AudioTracksSorting.DateAdded -> "date"
                        else -> "artist"
                    }
                )
                .putBoolean(KEY_ORDER,sorting.ascending)
                .apply()

            emitter.onSuccess(AppResult.Success(Unit))
        }
            .onErrorReturn { AppResult.Error(com.diskin.alon.sonix.common.application.AppError.DEVICE_STORAGE) }
            .subscribeOn(Schedulers.io())
    }
}