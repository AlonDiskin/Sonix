package com.diskin.alon.sonix.catalog.data

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import com.diskin.alon.sonix.catalog.application.util.AppResult
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Base class for implementations that provide public media items from user device.
 */
abstract class DeviceMediaStore(
    protected val contentResolver: ContentResolver,
    protected val contentUri: Uri,
    private val errorHandler: ContentResolverErrorHandler
) {

    protected fun <T : Any> query(query: () -> (T)): Observable<AppResult<T>> {
        return Observable.create<AppResult<T>> { emitter ->
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    emitter.onNext(AppResult.Success(query.invoke()))
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
            emitter.onNext(AppResult.Success(query.invoke()))

        }.subscribeOn(Schedulers.io())
            .onErrorReturn { AppResult.Error(errorHandler.handle(it)) }
            .startWith(AppResult.Loading())
    }

    fun delete(id: Int): Single<AppResult<Unit>> {
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