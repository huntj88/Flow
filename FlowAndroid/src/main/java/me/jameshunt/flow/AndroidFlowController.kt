package me.jameshunt.flow

import kotlinx.coroutines.CancellationException

abstract class AndroidFlowController<Input, Output> : FlowController<Input, FlowResult<Output>>() {

    interface BackState

    internal abstract suspend fun resume()

    internal abstract fun handleBack()

    // todo: get rid of this method
    protected suspend fun <Result, From> FlowResult<Result>.forResult(
        onBack: suspend () -> From = { throw NotImplementedError("onBack") },
        onComplete: suspend (Result) -> From = { throw NotImplementedError("onComplete") }
    ): From = when (this) {
        is FlowResult.Completed -> onComplete(this.data)
        is FlowResult.Back -> onBack()
    }

    protected suspend fun <Result, From> (suspend () -> FlowResult<Result>).forResult(
        onBack: suspend () -> From = { throw NotImplementedError("onBack") },
        onComplete: suspend (Result) -> From = { throw NotImplementedError("onComplete") },
        onRecover: suspend (Throwable) -> From = { throw RuntimeException("onRecover not implemented", it) }
    ): From = try {
        when (val results = this@forResult()) {
            is FlowResult.Completed -> onComplete(results.data)
            is FlowResult.Back -> onBack()
        }
    } catch (t: Throwable) {
        when(t) {
            is CancellationException -> onBack() // result discarded, another flow resolved sooner, see awaitFirst()
            else -> onRecover(t)
        }
    }
}