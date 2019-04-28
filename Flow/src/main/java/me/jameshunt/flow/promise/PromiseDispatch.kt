package me.jameshunt.flow.promise

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*

/**
 * Created by bghoward on 9/1/17.
 */

enum class PromiseDispatch {

    MAIN, BACKGROUND;

    companion object {
        private val main: Executor by lazy { Executor { command -> Handler(Looper.getMainLooper()).post(command) } }
        private val background: Executor by lazy { Executors.newCachedThreadPool() }
    }

    val executor: Executor
        get() = when (this) {
            MAIN -> main
            BACKGROUND -> background
        }
}

fun <T, OUT> Promise<OUT>.thenp(on: PromiseDispatch? = null, execute: (OUT) -> Promise<T>): Promise<T> =
    this.thenp(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <OUT> Promise<OUT>.recoverp(on: PromiseDispatch? = null, execute: (Exception) -> Promise<OUT>): Promise<OUT> =
    this.recoverp(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <T, OUT> Promise<OUT>.then(on: PromiseDispatch? = null, execute: (OUT) -> T): Promise<T> =
    this.then(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <OUT> Promise<OUT>.recover(on: PromiseDispatch? = null, execute: (Exception) -> OUT): Promise<OUT> =
    this.recover(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <OUT> Promise<OUT>.catch(on: PromiseDispatch? = null, execute: (Exception) -> Unit): Promise<OUT> =
    this.catch(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <OUT> Promise<OUT>.always(on: PromiseDispatch? = null, execute: () -> Unit): Promise<OUT> =
    this.always(on = (on ?: PromiseDispatch.MAIN).executor, execute = execute)

fun <T> Promise<T>.doAlso(on: PromiseDispatch? = null, also: (T) -> Unit): Promise<T> =
    this.then(on = on) { result -> result.also { also(result) } }

fun <T> List<Promise<T>>.firstToResolve(): Promise<T> = Promise.firstToResolve(this)