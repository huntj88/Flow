package me.jameshunt.flow3.splash

import me.jameshunt.flow.DeepLinkData
import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.ViewId
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.then

abstract class GeneratedSplashController(viewId: ViewId): FragmentFlowController<DeepLinkData, Unit>(viewId) {

    protected sealed class SplashFlowState: State {
        interface FromSplash
        interface FromLoad
        interface FromFinishedLoading

        object Back: SplashFlowState(), BackState
        data class Done(override val output: Unit): SplashFlowState(), DoneState<Unit>, FromFinishedLoading

        data class Splash(val deepLinkData: DeepLinkData): SplashFlowState()
        object Load: SplashFlowState(), FromSplash
        object FinishedLoading: SplashFlowState(), FromLoad
    }

    protected abstract fun onSplash(state: SplashFlowState.Splash): Promise<SplashFlowState.FromSplash>
    protected abstract fun onLoad(state: SplashFlowState.Load): Promise<SplashFlowState.FromLoad>
    protected abstract fun onFinishedLoading(state: SplashFlowState.FinishedLoading): Promise<SplashFlowState.FromFinishedLoading>

    final override fun onStart(state: InitialState<DeepLinkData>) {
        toSplash(SplashFlowState.Splash(deepLinkData = state.input))
    }

    private fun toSplash(state: SplashFlowState.Splash) {
        currentState = state
        onSplash(state).then {
            when(it) {
                is SplashFlowState.Load -> toLoad(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toLoad(state: SplashFlowState.Load) {
        currentState = state
        onLoad(state).then {
            when(it) {
                is SplashFlowState.FinishedLoading ->toFinishedLoading(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toFinishedLoading(state: SplashFlowState.FinishedLoading) {
        currentState = state
        onFinishedLoading(state).then {
            when(it) {
                is SplashFlowState.Done -> it.onDone()
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    final override fun resume(currentState: State) {
        when (currentState) {
            is InitialState<*> -> this.onStart(currentState as InitialState<DeepLinkData>)
            is SplashFlowState -> currentState.resumeState()
            else -> throw IllegalStateException("State is not part of this flow")
        }
    }

    private fun SplashFlowState.resumeState() {
        when(this) {
            is SplashFlowState.Splash -> this@GeneratedSplashController.onSplash(this)
            is SplashFlowState.Load -> this@GeneratedSplashController.onLoad(this)
            is SplashFlowState.FinishedLoading -> this@GeneratedSplashController.onFinishedLoading(this)
        }
    }
}