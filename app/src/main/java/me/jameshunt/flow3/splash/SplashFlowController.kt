package me.jameshunt.flow3.splash

import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.ViewId
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.PromiseDispatch
import me.jameshunt.flow.promise.then
import me.jameshunt.flow.proxy
import me.jameshunt.flow3.splash.GeneratedSplashController.SplashFlowState.*

class SplashFlowController(viewId: ViewId): GeneratedSplashController(viewId) {

    private val splashFragmentProxy = proxy(SplashFragment::class.java)

    override fun onSplash(state: Splash): Promise<FromSplash> {
        return this.flow(fragmentProxy = splashFragmentProxy, input = Unit).forResult<Unit, FromSplash>(
            onBack = { Promise(Back) },
            onComplete = { Promise(Load) }
        )
    }

    override fun onLoad(state: Load): Promise<FromLoad> {
        return Promise(Unit)
            .then(on = PromiseDispatch.BACKGROUND) { Thread.sleep(1000) }
            .then { Done(Unit) }
    }

}

abstract class GeneratedSplashController(viewId: ViewId): FragmentFlowController<Unit, Unit>(viewId) {

    protected sealed class SplashFlowState: State {
        interface FromSplash
        interface FromLoad

        object Back: SplashFlowState(), BackState, FromSplash
        data class Done(override val output: Unit): SplashFlowState(), DoneState<Unit>, FromLoad

        object Splash: SplashFlowState()
        object Load: SplashFlowState(), FromSplash
    }

    protected abstract fun onSplash(state: Splash): Promise<FromSplash>
    protected abstract fun onLoad(state: Load): Promise<FromLoad>

    final override fun onStart(state: InitialState<Unit>) {
        toSplash(Splash)
    }

    private fun toSplash(state: Splash) {
        currentState = state
        onSplash(state).then {
            when(it) {
                is Load -> toLoad(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toLoad(state: Load) {
        currentState = state
        onLoad(state).then {
            when(it) {
                is Done -> it.onDone()
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    final override fun resume(currentState: State) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}