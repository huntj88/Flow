//package com.inmotionsoftware.promise

package me.jameshunt.flow

// not actually my code

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by bghoward on 8/29/17.
 *
 */

fun Executor?.dispatch(execute: () -> Unit ) {
    this?.execute(execute) ?: execute()
}

open class Promise<OUT> {
    sealed class Result<T> {
        class Resolved<T>(val value: T) : Result<T>()
        class Rejected<T>(val error: Throwable) : Result<T>()

        var errorOrNull: Throwable? = (this as? Rejected)?.error
        var valueOrNull: T? = (this as? Resolved)?.value
    }

    sealed class Status<T> {
        class Resolved<T>(val value: T) : Status<T>()
        class Rejected<T>(val error: Throwable) : Status<T>()
        class Pending<T>: Status<T>()

        val isRejected get() = this is Rejected<T>
        val isResolved get() = this is Resolved<T>
        val isFulfilled get() = !this.isPending
        val isPending get() = this is Pending<T>
    }

    protected interface Input<IN> {
        fun resolve(result: Status<IN>)
    }

    private interface Output<OUT> {
        fun add(child: Input<OUT>)

        var status: Status<OUT> get
    }

    protected abstract class Continuation<IN, OUT>(private val executor: Executor?) : Input<IN>,
        Output<OUT> {
        private val children: MutableList<Input<OUT>> by lazy { mutableListOf<Input<OUT>>() }

        override var status: Status<OUT> =
            Status.Pending()
            get() = synchronized(this) { return field }
            set(value) {
                // can't set to pending
                if (value is Status.Pending) { return }

                val list = synchronized(this) {
                    // can only be set once!
                    if (field !is Status.Pending) { return }
                    field = value

                    if (this.children.size > 0) {
                        val list = mutableListOf<Input<OUT>>()
                        list.addAll(this.children)
                        this.children.clear()
                        list
                    } else {
                        null
                    }
                }

                list?.forEach { it.resolve(status) }
            }

        private fun execute(value: IN): Status<OUT> {
            return try {
                val out = dispatch(value)
                Status.Resolved(out)
            } catch (err: Throwable) {
                try {
                    recover(err)
                } catch (err2: Throwable) {
                    Status.Rejected(err2)
                }
            }
        }

        override fun resolve(result: Status<IN>) {
            if (result is Status.Pending) { return }
            executor.dispatch {
                try {
                    when (result) {
                        is Status.Resolved -> this.status = execute(result.value)
                        is Status.Rejected -> this.status = recover(result.error)
                    }
                } catch (t: Throwable) { this.status = Status.Rejected(t)
                } finally { always() }
            }
        }

        abstract fun dispatch(value: IN): OUT
        open fun always() {}
        open fun recover(error: Throwable): Status<OUT> =
            Status.Rejected(error)

        override fun add(child: Input<OUT>) {
            val result = synchronized(this) {
                when(this.status) {
                    is Status.Pending -> { children.add(child); null }
                    is Status.Rejected -> this.status
                    is Status.Resolved -> this.status
                }
            }

            result?.let { child.resolve(it) }
        }
    }

    private class BasicContinuation<T,RT>(
                executor: Executor? = null,
                val then: (T) -> RT,
                val recover: ((Throwable) -> RT)? = null,
                val always: (() -> Unit)? = null)
            : Continuation<T, RT>(executor) {

        override fun dispatch(value: T): RT = then(value)
        override fun recover(error: Throwable): Status<RT> {
            val recover = this.recover ?: return Status.Rejected(error)
            return try {
                Status.Resolved(recover(error))
            } catch (err: Throwable) {
                Status.Rejected(err)
            }
        }

        override fun always() { this.always?.invoke() }
    }

    private class DeferredContinuation<T>(executor: Executor? = null) : Continuation<T, T>(executor) {
        override fun dispatch(value: T): T = value
        fun resolve(value: T) = resolve(Status.Resolved(value))
        fun reject(error: Throwable) = resolve(Status.Rejected(error))
    }

    protected class ResolvedContinuation<T>(value: Status<T>) : Continuation<T, T>(null) {
        init {
            this.status = value
        }

        // Should never get called...
        override fun dispatch(value: T): T = throw IllegalStateException()
    }


    private var output: Output<OUT>

    val status: Status<OUT> get() = this.output.status
    val isPending: Boolean get() = this.status.isPending
    val isFulfilled: Boolean get() = this.status.isFulfilled
    val isRejected: Boolean get() = this.status.isRejected
    val isResolved: Boolean get() = this.status.isResolved

    constructor(on: Executor? = null, execute: ((OUT) -> Unit, (Throwable) -> Unit) -> Unit) {
        val cont = DeferredContinuation<OUT>()
        this.output = cont

        on.dispatch {
            execute({ cont.resolve(it) }, { cont.reject(it) })
        }
    }

    private constructor(output: Output<OUT>) {
        this.output = output
    }

    constructor(value: OUT) {
        this.output = ResolvedContinuation(
            Status.Resolved(value)
        )
    }

    constructor(error: Throwable) {
        this.output = ResolvedContinuation(
            Status.Rejected(error)
        )
    }

    fun recover(on: Executor?, execute: (Throwable) -> OUT): Promise<OUT> {
        val cont = BasicContinuation<OUT, OUT>(
            executor = on,
            then = { it },
            recover = execute
        )
        this.output.add(cont)
        return Promise(cont)
    }

    fun <T> then(on: Executor?, execute: (OUT) -> T): Promise<T> {
        val cont = BasicContinuation(executor = on, then = execute)
        this.output.add(cont)
        return Promise(cont)
    }

    fun catch(on: Executor?, execute: (Throwable) -> Unit): Promise<OUT> {
        val cont =
            BasicContinuation<OUT, Unit>(executor = on, then = {}, recover = execute)
        this.output.add(cont)
        return this
    }

    fun always(on: Executor?, execute: () -> Unit ): Promise<OUT> {
        val cont =
            BasicContinuation<OUT, Unit>(executor = on, then = {}, always = execute)
        this.output.add(cont)
        return this
    }

    fun asVoid(): Promise<Unit> = this.then(on=null) { Unit }

    fun <T> thenp(on: Executor?, execute: (OUT) -> Promise<T>): Promise<T> {
        val outer = BasicContinuation(executor = on, then = execute)
        this.output.add(outer)
        val output = BasicContinuation<T, T>(then = { it })
        val inner =
            BasicContinuation<Promise<T>, Unit>(then = {
                it.output.add(output)
            }, recover = { output.resolve(Status.Rejected(it)) })
        outer.add(inner)
        return Promise(output)
    }

    fun recoverp(on: Executor?, execute: (Throwable) -> Promise<OUT>): Promise<OUT> {
        val outer = BasicContinuation<OUT, Promise<OUT>>(
            executor = on,
            then = { Promise(it) },
            recover = execute
        )
        this.output.add(outer)
        val output = BasicContinuation<OUT, OUT>(then = { it })
        val inner =
            BasicContinuation<Promise<OUT>, Unit>(then = {
                it.output.add(output)
            }, recover = { output.resolve(Status.Rejected(it)) })
        outer.add(inner)
        return Promise(output)
    }

    companion object {
        fun <T> deferred(): DeferredPromise<T> = DeferredPromise()
        fun void(): Promise<Unit> = Promise(Unit)

        fun <T> join(promises: Iterable<Promise<T>>): Promise<Iterable<T>> {
            return resolveParallel(promises).then(on=null) {
                // did we have an error?
                for (result in it) {
                    if (result is Result.Rejected) { throw result.error }
                }
                it.map { (it as Result.Resolved).value }
            }
        }

        fun <T> resolveParallel(promises: Iterable<Promise<T>>): Promise<Iterable<Result<T>>> {
            if (!promises.iterator().hasNext()) { return Promise(value = emptyList())
            }

            return Promise { resolve, _ ->

                val results = mutableListOf<Result<T>>()
                val count = AtomicInteger(promises.count())
                promises.forEach {
                    it.always(on = null) {
                        val status = it.output.status
                        val result: Result<T> = when (status) {
                            is Status.Pending -> Result.Rejected(
                                IllegalStateException()
                            )
                            is Status.Rejected -> Result.Rejected(
                                status.error
                            )
                            is Status.Resolved -> Result.Resolved(
                                status.value
                            )
                        }
                        results.add(result)

                        if (count.decrementAndGet() == 0) {
                            resolve(results)
                        }
                    }
                }
            }
        }

        fun <T> resolveSerial(promises: Iterable<Promise<T>>): Promise<Iterable<T>> {
            val it = promises.iterator()
            if (!it.hasNext()) { return Promise(emptyList())
            }

            val rt = mutableListOf<T>()
            var prev = it.next()
            while (it.hasNext()) {
                val next = it.next()
                prev = prev.thenp(on=null) {
                    rt.add(it)
                    next
                }
            }

            return prev.then(on=null) {
                rt.add(it)
                rt
            }
        }

        fun <T> reduce(fulfilled: Collection<Promise<T>>): Promise<Unit> = resolveSerial(
            fulfilled
        ).asVoid()
    }
}

class DeferredPromise<T> {
    val promise: Promise<T>
    private var _resolve: ((T) -> Unit)? = null
    private var _reject: ((Throwable) -> Unit)? = null

    init {
        promise = Promise { resolve, reject ->
            this._resolve = resolve
            this._reject = reject
        }
    }

    fun resolve(value: T) {
        this._resolve?.let { it(value) }
    }

    fun reject(error: Throwable) {
        this._reject?.let{ it(error) }
    }
}

fun <T> join(promises: Iterable<Promise<T>>): Promise<Iterable<T>> =
    Promise.join(promises)