package me.jameshunt.flow3.splash

import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.map
import me.jameshunt.flow.DeepLinkData
import me.jameshunt.flow.FragmentFlowController

abstract class GeneratedSplashController: FragmentFlowController<DeepLinkData, Unit>() {

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
        onSplash(state).map {
            when(it) {
                is SplashFlowState.Load -> toLoad(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toLoad(state: SplashFlowState.Load) {
        currentState = state
        onLoad(state).map {
            when(it) {
                is SplashFlowState.FinishedLoading ->toFinishedLoading(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toFinishedLoading(state: SplashFlowState.FinishedLoading) {
        currentState = state
        onFinishedLoading(state).map {
            when(it) {
                is SplashFlowState.Done -> it.onDone()
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }
}