package me.jameshunt.flow

abstract class AndroidFlowController<Input, Output> : FlowController<Input, FlowResult<Output>>() {

    internal abstract fun resume()

    internal abstract fun handleBack()

    // Inlining this gives better errors about where the error happened
    protected fun <Result, From> FlowResult<Result>.forResult(
        onBack: () -> From = { throw NotImplementedError("onBack") },
        onComplete: (Result) -> From = { throw NotImplementedError("onComplete") },
        onCatch: ((Throwable) -> From) = { throw it }
    ): From {
        return when (this) {
            is FlowResult.Back -> onBack()
            is FlowResult.Completed -> onComplete(this.data)
            // TODO: catch
        }
    }
}