package com.diskin.alon.sonix.catalog.data

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.diskin.alon.sonix.catalog.application.interfaces.ArtistsSortingStore
import com.diskin.alon.sonix.catalog.application.model.ArtistSorting
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Stores [ArtistSorting] to local storage and provide retrieval of last stored value.
 */
class ArtistsSortingStoreImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ArtistsSortingStore {

    companion object {
        @VisibleForTesting
        const val KEY_SORTING = "artists_sorting"
        @VisibleForTesting
        const val KEY_ORDER = "albums_order"
        @VisibleForTesting
        const val ARTIST_NAME_SORTING: String = "name"
        @VisibleForTesting
        const val DATE_ADDED_SORTING: String = "date"
        @VisibleForTesting
        const val DEF_SORTING: String = ARTIST_NAME_SORTING
        @VisibleForTesting
        const val DEF_ORDER = true
    }

    override fun getLast(): Single<AppResult<ArtistSorting>> {
        return Single.create<AppResult<ArtistSorting>> { emitter ->
            val lastSorting = when (sharedPreferences.getString(KEY_SORTING, DEF_SORTING)) {
                ARTIST_NAME_SORTING ->
                    ArtistSorting.Name(sharedPreferences.getBoolean(KEY_ORDER, DEF_ORDER))
                else ->
                    ArtistSorting.Date(sharedPreferences.getBoolean(KEY_ORDER, DEF_ORDER))
            }

            emitter.onSuccess(AppResult.Success(lastSorting))
        }
            .onErrorReturn { AppResult.Error(AppError.DEVICE_STORAGE) }
            .subscribeOn(Schedulers.io())
    }

    override fun save(sorting: ArtistSorting): Single<AppResult<Unit>> {
        return Single.create<AppResult<Unit>> { emitter ->
            sharedPreferences.edit()
                .putString(
                    KEY_SORTING,
                    when(sorting){
                        is ArtistSorting.Name -> ARTIST_NAME_SORTING
                        else -> DATE_ADDED_SORTING
                    }
                )
                .putBoolean(KEY_ORDER,sorting.ascending)
                .apply()

            emitter.onSuccess(AppResult.Success(Unit))
        }
            .onErrorReturn { AppResult.Error(AppError.DEVICE_STORAGE) }
            .subscribeOn(Schedulers.io())
    }
}