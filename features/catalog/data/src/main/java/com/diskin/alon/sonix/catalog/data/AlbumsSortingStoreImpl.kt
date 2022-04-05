package com.diskin.alon.sonix.catalog.data

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.diskin.alon.sonix.catalog.application.interfaces.AlbumsSortingStore
import com.diskin.alon.sonix.catalog.application.model.AlbumSorting
import com.diskin.alon.sonix.common.application.AppError
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Stores [AlbumSorting] to local storage and provide retrieval of last stored value.
 */
class AlbumsSortingStoreImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : AlbumsSortingStore {

    companion object {
        @VisibleForTesting
        const val KEY_SORTING = "albums_sorting"
        @VisibleForTesting
        const val KEY_ORDER = "albums_order"
        @VisibleForTesting
        const val ARTIST_SORTING: String = "artist"
        @VisibleForTesting
        const val ALBUM_NAME_SORTING: String = "name"
        @VisibleForTesting
        const val DEF_SORTING: String = ALBUM_NAME_SORTING
        @VisibleForTesting
        const val DEF_ORDER = true
    }

    override fun getLast(): Single<AppResult<AlbumSorting>> {
        return Single.create<AppResult<AlbumSorting>> { emitter ->
            val lastSorting = when (sharedPreferences.getString(KEY_SORTING, DEF_SORTING)) {
                ALBUM_NAME_SORTING ->
                    AlbumSorting.Name(sharedPreferences.getBoolean(KEY_ORDER, DEF_ORDER))
                else ->
                    AlbumSorting.Artist(sharedPreferences.getBoolean(KEY_ORDER, DEF_ORDER))
            }

            emitter.onSuccess(AppResult.Success(lastSorting))
        }
            .onErrorReturn { AppResult.Error(AppError.DEVICE_STORAGE) }
            .subscribeOn(Schedulers.io())
    }

    override fun save(sorting: AlbumSorting): Single<AppResult<Unit>> {
        return Single.create<AppResult<Unit>> { emitter ->
            sharedPreferences.edit()
                .putString(
                    KEY_SORTING,
                    when(sorting){
                        is AlbumSorting.Name -> ALBUM_NAME_SORTING
                        else -> ARTIST_SORTING
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