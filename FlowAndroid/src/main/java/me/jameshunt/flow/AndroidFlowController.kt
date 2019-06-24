package me.jameshunt.flow

abstract class AndroidFlowController<Input, Output> : FlowController<Input, FlowResult<Output>>() {

    interface BackState

    internal abstract suspend fun resume()

    internal abstract fun handleBack()

    // Inlining this gives better errors about where the error happened
//    protected inline fun <Result, From> Promise<FlowResult<Result>>.forResult(
//        crossinline onBack: () -> Promise<From> = { throw NotImplementedError("onBack") },
//        crossinline onComplete: (Result) -> Promise<From> = { throw NotImplementedError("onComplete") },
//        crossinline onCatch: ((Throwable) -> Promise<From>) = { throw it }
//    ): Promise<From> = this
//        .thenMap {
//            when (it) {
//                is FlowResult.Back -> onBack()
//                is FlowResult.Completed -> onComplete(it.data)
//            }
//        }
//        .recover { onCatch(it) }

    protected suspend fun <Result, From> FlowResult<Result>.forResult(
        onBack: suspend () -> From = { throw NotImplementedError("onBack") },
        onComplete: suspend (Result) -> From = { throw NotImplementedError("onComplete") }
    ): From = when(this) {
        is FlowResult.Completed -> onComplete(this.data)
        is FlowResult.Back -> onBack()
    }
}