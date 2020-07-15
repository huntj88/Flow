package me.jameshunt.flow

import kotlinx.coroutines.CompletableDeferred

abstract class FlowController<Input, Output> {

    interface State

    data class InitialState<Input>(val input: Input) : State

    @Deprecated("useless interface, still in generated code")
    interface DoneState<Output> {
        val output: Output
    }

    private val resultPromise: CompletableDeferred<Output> = CompletableDeferred()

    val childFlows: MutableList<FlowController<*, *>> = mutableListOf()

    protected abstract fun onStart(state: InitialState<Input>)

    protected fun onDone(output: Output) {
        this.resultPromise.complete(output)
    }

    protected fun onCatch(e: Throwable) {
        this.resultPromise.completeExceptionally(e)
    }

    open suspend fun launchFlow(input: Input): CompletableDeferred<Output> {
        this.onStart(InitialState(input))
        return this.resultPromise
    }
}
