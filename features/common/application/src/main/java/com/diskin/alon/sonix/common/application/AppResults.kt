package com.diskin.alon.sonix.common.application

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

enum class AppError {
    UNKNOWN_ERROR,DB_ERROR,DEVICE_STORAGE,PLAYER_ERROR,INTERNAL_ERROR
}

sealed class AppResult<R : Any> {

    data class Success<R : Any>(val data: R): AppResult<R>()

    data class Error<R : Any>(val error: AppError): AppResult<R>()

    class Loading<R : Any> : AppResult<R>()
}

fun <T : Any, R : Any> Observable<AppResult<T>>.mapAppResult(mapper: Function<T, R>): Observable<AppResult<R>> {
    return this.map {
        when(it) {
            is AppResult.Success -> AppResult.Success(
                mapper.apply(
                    it.data
                )
            )
            else -> it as AppResult<R>
        }
    }
}

fun <T : Any, R : Any> Single<AppResult<T>>.mapAppResult(mapper: Function<T, R>): Single<AppResult<R>> {
    return this.map {
        when(it) {
            is AppResult.Success -> AppResult.Success(
                mapper.apply(
                    it.data
                )
            )
            else -> it as AppResult<R>
        }
    }
}

fun <T : Any, R : Any> Observable<AppResult<T>>.flatMapAppResult(mapper: (T) -> (Observable<AppResult<R>>)): Observable<AppResult<R>> {
    return this.flatMap {
        when(it) {
            is AppResult.Success -> mapper.invoke(it.data)
            is AppResult.Error -> Observable.just(
                AppResult.Error(
                    it.error
                )
            )
            is AppResult.Loading -> Observable.just(AppResult.Loading())
        }
    }
}

fun <T : Any> Observable<T>.toAppResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.map { toSuccessAppResult(it) }
        .onErrorReturn { toAppResultError(it,errorHandler) }
}

fun <T : Any> Observable<T>.toIOLoadingAppResult(errorHandler: ((Throwable) -> (AppError))? = null): Observable<AppResult<T>> {
    return this.subscribeOn(Schedulers.io())
        .map { toSuccessAppResult(it) }
        .onErrorReturn { toAppResultError(it,errorHandler) }
        .startWith(AppResult.Loading())
}

private fun <T : Any> toSuccessAppResult(data: T): AppResult<T> {
    return AppResult.Success(data)
}

private fun <T : Any> toAppResultError(throwable: Throwable, errorHandler: ((Throwable) -> (AppError))? = null): AppResult<T> {
    return AppResult.Error(errorHandler?.invoke(throwable) ?: AppError.UNKNOWN_ERROR)
}