package me.jameshunt.flow

import com.inmotionsoftware.promisekt.DeferredPromise
import com.inmotionsoftware.promisekt.Promise

abstract class FlowController<Input, Output> {

    interface State

    data class InitialState<Input>(val input: Input) : State

    interface DoneState<Output> {
        val output: Output
    }

    private val resultPromise: DeferredPromise<Output> = DeferredPromise()

    val childFlows: MutableList<FlowController<*, *>> = mutableListOf()

    protected abstract fun onStart(state: InitialState<Input>)

    protected fun onDone(output: Output) {
        this.resultPromise.resolve(output)
    }

    protected fun onCatch(e: Throwable) {
        this.resultPromise.reject(e)
    }

    open fun launchFlow(input: Input): Promise<Output> {
        this.onStart(InitialState(input))
        return this.resultPromise.promise
    }
}
