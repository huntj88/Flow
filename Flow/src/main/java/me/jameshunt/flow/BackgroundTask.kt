package me.jameshunt.flow

import com.inmotionsoftware.promisekt.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface BackgroundTask {

    companion object {
        private val backgroundExecutor: Executor? = Executors.newCachedThreadPool()
    }

    fun <T, U> Promise<T>.map(map: (T) -> U): Promise<U> =
        this.map(on = backgroundExecutor) { map(it) }

    fun <T> Promise<T>.ensure(ensure: () -> Unit): Promise<T> =
        this.ensure(on = backgroundExecutor) { ensure() }

    fun <T> Promise<T>.done(done: () -> Unit): Promise<Unit> =
        this.done(on = backgroundExecutor) { done() }

    fun <T> Promise<T>.then(then: (T) -> Promise<T>): Promise<T> =
        this.then(on = backgroundExecutor) { then(it) }

    fun <T, U> Promise<T>.thenMap(thenMap: (T) -> Promise<U>): Promise<U> =
        this.thenMap(on = backgroundExecutor) { thenMap(it) }

    fun <T> Promise<T>.recover(recover: (Throwable) -> Promise<T>): Promise<T> =
        this.recover(on = backgroundExecutor) { recover(it) }

    fun <T> Promise<T>.catch(catch: (Throwable) -> Unit): PMKFinalizer =
        this.catch(on = backgroundExecutor) { catch(it) }

    fun PMKFinalizer.finally(finally: () -> Unit) =
        this.finally(on = backgroundExecutor) { finally() }
}