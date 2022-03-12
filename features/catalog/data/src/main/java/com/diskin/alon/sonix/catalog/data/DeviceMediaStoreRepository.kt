package com.diskin.alon.sonix.catalog.data

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import com.diskin.alon.sonix.common.application.AppResult
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMediaStoreRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val errorHandler: ContentResolverErrorHandler
) {

    fun <T : Any> query(contentUri: Uri,query: (contentResolver: ContentResolver) -> (T)): Observable<AppResult<T>> {
        return Observable.create<AppResult<T>> { emitter ->
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    emitter.onNext(AppResult.Success(query.invoke(contentResolver)))
                }
            }

            // Register a pictures content listener
            contentResolver.registerContentObserver(
                contentUri,
                true,
                observer
            )

            // Unregister when observable canceled
            emitter.setCancellable { contentResolver.unregisterContentObserver(observer) }

            // Perform query
            emitter.onNext(AppResult.Success(query.invoke(contentResolver)))

        }.subscribeOn(Schedulers.io())
            .onErrorReturn { AppResult.Error(errorHandler.handle(it)) }
            .startWith(AppResult.Loading())
    }

    fun delete(contentUri: Uri,id: Int): Single<AppResult<Unit>> {
        return Single.create<AppResult<Unit>> { emitter ->
            contentResolver.delete(
                Uri.parse(
                    contentUri.toString()
                        .plus("/$id")
                ),
                null,
                null
            )

            emitter.onSuccess(AppResult.Success(Unit))

        }.subscribeOn(Schedulers.io())
            .onErrorReturn { AppResult.Error(errorHandler.handle(it)) }
    }
}