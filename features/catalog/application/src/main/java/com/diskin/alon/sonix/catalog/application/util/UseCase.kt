package com.diskin.alon.sonix.catalog.application.util

/**
 * Application use case contract.
 *
 * @param P use case input type.
 * @param R use case result type.
 */
interface UseCase<P : Any,R : Any> {

    /**
     * Execute use case.
     */
    fun execute(param: P): R
}