package com.diskin.alon.sonix.catalog.data

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import com.diskin.alon.sonix.catalog.application.util.AppResult
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Base class for implementations that provide public media items from user device.
 */
abstract class DeviceMediaStore(
    protected val contentResolver: ContentResolver,
    protected val contentUri: Uri,
    private val errorHandler: ContentResolverErrorHandler
) {

    protected fun <T : Any> query(query: () -> (T)): Observable<com.diskin.alon.sonix.catalog.application.util.AppResult<T>> {
        return Observable.create<com.diskin.alon.sonix.catalog.application.util.AppResult<T>> { emitter ->
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    emitter.onNext(com.diskin.alon.sonix.catalog.application.util.AppResult.Success(query.invoke()))
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
            emitter.onNext(com.diskin.alon.sonix.catalog.application.util.AppResult.Success(query.invoke()))

        }.subscribeOn(Schedulers.io())
            .onErrorReturn { com.diskin.alon.sonix.catalog.application.util.AppResult.Error(errorHandler.handle(it)) }
            .startWith(com.diskin.alon.sonix.catalog.application.util.AppResult.Loading())
    }
}