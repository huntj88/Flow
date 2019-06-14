package me.jameshunt.flow

import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.recover
import com.inmotionsoftware.promisekt.thenMap

abstract class AndroidFlowController<Input, Output> : FlowController<Input, FlowResult<Output>>() {
    interface BackState

    protected lateinit var currentState: State

    internal fun resume() = resume(currentState)
    internal abstract fun resume(currentState: State)

    internal abstract fun handleBack()

    // Inlining this gives better errors about where the error happened
    inline fun <Result, From> Promise<FlowResult<Result>>.forResult(
        crossinline onBack: () -> Promise<From> = { throw NotImplementedError("onBack") },
        crossinline onComplete: (Result) -> Promise<From> = { throw NotImplementedError("onComplete") },
        crossinline onCatch: ((Throwable) -> Promise<From>) = { throw it }
    ): Promise<From> = this
        .thenMap {
            when (it) {
                is FlowResult.Back -> onBack()
                is FlowResult.Completed -> onComplete(it.data)
            }
        }
        .recover { onCatch(it) }
}