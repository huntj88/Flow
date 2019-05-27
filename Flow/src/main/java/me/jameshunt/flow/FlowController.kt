package me.jameshunt.flow

import com.inmotionsoftware.promisekt.DeferredPromise
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.recover
import com.inmotionsoftware.promisekt.thenMap

abstract class FlowController<Input, Output> {

    interface State

    interface BackState

    data class InitialState<Input>(val input: Input) : State

    protected lateinit var currentState: State

    private val resultPromise: DeferredPromise<FlowResult<Output>> = DeferredPromise()

    internal val childFlows: MutableList<FlowController<*, *>> = mutableListOf()

    protected abstract fun onStart(state: InitialState<Input>)

    protected abstract fun resume(currentState: State)

    internal fun resume() = resume(currentState)

    internal abstract fun handleBack()

    protected fun BackState.onBack() {
        this@FlowController.resultPromise.resolve(FlowResult.Back)
    }

    internal fun onDone(output: Output) {
        this.resultPromise.resolve(FlowResult.Completed(output))
    }

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

    // internal to this instance use
    internal fun launchFlow(input: Input): Promise<FlowResult<Output>> {
        currentState = InitialState(input)
        this.onStart(currentState as InitialState<Input>)
        return this.resultPromise.promise
    }
}