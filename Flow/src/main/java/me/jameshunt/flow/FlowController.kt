package me.jameshunt.flow

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class FlowController<Input, Output> {

    interface State

    data class InitialState<Input>(val input: Input) : State

    interface DoneState<Output> {
        val output: Output
    }

    private val outputDeferred: CompletableDeferred<Output> = CompletableDeferred()

    val childFlows: MutableList<FlowController<*, *>> = mutableListOf()

    protected abstract suspend fun onStart(state: InitialState<Input>)

    protected fun onDone(output: Output) {
        outputDeferred.complete(output)
    }

    protected fun onCatch(e: Throwable) {
        outputDeferred.completeExceptionally(e)
    }

    open suspend fun launchFlow(input: Input): Output {
        CoroutineScope(Dispatchers.Main).launch {
            onStart(InitialState(input))
        }
        return outputDeferred.await()
    }
}
