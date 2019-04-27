package me.jameshunt.flow

import me.jameshunt.flow.promise.*

typealias ViewId = Int

abstract class FlowController<Input, Output> {

    interface State

    interface BackState

    data class InitialState<Input>(val input: Input) : State

    protected lateinit var currentState: State

    private val resultPromise: DeferredPromise<FlowResult<Output>> =
        DeferredPromise()

    internal val childFlows: MutableList<FlowController<*, *>> = mutableListOf()

    protected abstract fun onStart(state: InitialState<Input>)

    protected abstract fun resume(currentState: State)

    fun resume() = resume(currentState)

    abstract fun handleBack()

    protected fun BackState.onBack() {
        this@FlowController.resultPromise.resolve(FlowResult.Back)
    }

    internal fun onDone(output: Output) {
        this.resultPromise.resolve(FlowResult.Completed(output))
    }

    fun <Result, From> Promise<FlowResult<Result>>.forResult(
        onBack: () -> Promise<From>,
        onComplete: (Result) -> Promise<From>,
        onCatch: ((Exception) -> Promise<From>) = { throw it }
    ): Promise<From> = this
        .thenp {
            when (it) {
                is FlowResult.Back -> onBack()
                is FlowResult.Completed -> onComplete(it.data)
            }
        }
        .recoverp { onCatch(it) }

    // internal to this instance use
    internal fun launchFlow(input: Input): Promise<FlowResult<Output>> {
        currentState = InitialState(input)
        this.onStart(currentState as InitialState<Input>)
        return this.resultPromise.promise
    }
}