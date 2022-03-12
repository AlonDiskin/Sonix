package com.diskin.alon.sonix.catalog.data

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.diskin.alon.sonix.catalog.application.interfaces.TracksSortingStore
import com.diskin.alon.sonix.catalog.application.model.AudioTracksSorting
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Stores [AudioTracksSorting] to local storage and provide retrieval of last stored value.
 */
class TracksSortingStoreImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : TracksSortingStore {

    companion object {
        @VisibleForTesting
        const val KEY_SORTING = "sorting"
        @VisibleForTesting
        const val KEY_ORDER = "order"
        @VisibleForTesting
        const val TRACK_DATE_SORTING = "date"
        @VisibleForTesting
        const val TRACK_ARTIST_SORTING = "artist"
        @VisibleForTesting
        const val DEF_SORTING: String = TRACK_DATE_SORTING
        @VisibleForTesting
        const val DEF_ORDER = false
    }

    override fun getLast(): Single<AppResult<AudioTracksSorting>> {
        return Single.create<AppResult<AudioTracksSorting>> { emitter ->
            val lastSorting = when (sharedPreferences.getString(KEY_SORTING, DEF_SORTING)) {
                TRACK_DATE_SORTING ->
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
                        is AudioTracksSorting.DateAdded -> TRACK_DATE_SORTING
                        else -> TRACK_ARTIST_SORTING
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