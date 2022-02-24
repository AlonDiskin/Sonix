package com.diskin.alon.sonix.catalog.data

import com.diskin.alon.sonix.common.application.AppError
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Content resolver error handler. maps content resolver errors to [AppError]s.
 */
@Singleton
class ContentResolverErrorHandler @Inject constructor() {

    fun handle(error: Throwable): com.diskin.alon.sonix.common.application.AppError {
        return com.diskin.alon.sonix.common.application.AppError.DEVICE_STORAGE
    }
}
