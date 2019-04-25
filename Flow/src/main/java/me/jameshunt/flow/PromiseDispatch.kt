package me.jameshunt.flow

import java.util.concurrent.*

/**
 * Created by bghoward on 9/1/17.
 */

enum class PromiseDispatch {

    MAIN, BACKGROUND;

    companion object {
        private lateinit var main: Executor
        private val background: Executor by lazy { Executors.newCachedThreadPool() }

        fun setMainExecutor(executor: Executor) {
            main = executor
        }
    }

    val executor: Executor
        get() = when (this) {
            MAIN -> main
            BACKGROUND -> background
        }
}

fun <T, OUT> Promise<OUT>.thenp(on: PromiseDispatch? = null, execute: (OUT) -> Promise<T>): Promise<T> =
    this.thenp(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <OUT> Promise<OUT>.recoverp(on: PromiseDispatch? = null, execute: (Throwable) -> Promise<OUT>): Promise<OUT> =
    this.recoverp(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <T, OUT> Promise<OUT>.then(on: PromiseDispatch? = null, execute: (OUT) -> T): Promise<T> =
    this.then(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <OUT> Promise<OUT>.recover(on: PromiseDispatch? = null, execute: (Throwable) -> OUT): Promise<OUT> =
    this.recover(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <OUT> Promise<OUT>.catch(on: PromiseDispatch? = null, execute: (Throwable) -> Unit): Promise<OUT> =
    this.catch(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <OUT> Promise<OUT>.always(on: PromiseDispatch? = null, execute: () -> Unit): Promise<OUT> =
    this.always(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)


fun <T> Promise<T>.doAlso(also: (T) -> Unit): Promise<T> = this.then { result -> result.also { also(result) } }
