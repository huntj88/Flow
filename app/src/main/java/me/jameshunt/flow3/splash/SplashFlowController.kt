package me.jameshunt.flow3.splash

import me.jameshunt.flow.*
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.PromiseDispatch
import me.jameshunt.flow.promise.then
import me.jameshunt.flow3.R
import me.jameshunt.flow3.ViewPagerGroupController
import me.jameshunt.flow3.root.RootFlowController
import me.jameshunt.flow3.splash.GeneratedSplashController.SplashFlowState.*

class SplashFlowController(viewId: ViewId): GeneratedSplashController(viewId) {

    private val splashFragmentProxy = proxy(SplashFragment::class.java)

    override fun onSplash(state: Splash): Promise<FromSplash> {
        return this.flow(fragmentProxy = splashFragmentProxy, input = Unit).forResult<Unit, FromSplash>(
            onBack = { TODO() },
            onComplete = { Promise(Load) }
        )
    }

    override fun onLoad(state: Load): Promise<FromLoad> {
        return Promise(Unit)
            .then(on = PromiseDispatch.BACKGROUND) { Thread.sleep(1000) }
            .then { FinishedLoading }
    }

    override fun onFinishedLoading(state: FinishedLoading): Promise<FromFinishedLoading> {
        val flowsInGroup = mapOf(
            RootFlowController::class.java.putInView(R.id.groupPagerZero),
            RootFlowController::class.java.putInView(R.id.groupPagerOne),
            RootFlowController::class.java.putInView(R.id.groupPagerTwo)
        )
        val groupArgs = FragmentGroupFlowController.FlowsInGroup(flowsInGroup, Unit)

        return this.flowGroup(ViewPagerGroupController::class.java, groupArgs).forResult<Unit, FromFinishedLoading>(
            onBack = { Promise(Done(Unit)) },
            onComplete = { Promise(Done(Unit)) }
        )
    }
}

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

    protected abstract fun onSplash(state: Splash): Promise<FromSplash>
    protected abstract fun onLoad(state: Load): Promise<FromLoad>
    protected abstract fun onFinishedLoading(state: FinishedLoading): Promise<FromFinishedLoading>

    final override fun onStart(state: InitialState<DeepLinkData>) {
        toSplash(Splash(deepLinkData = state.input))
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
                is FinishedLoading ->toFinishedLoading(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toFinishedLoading(state: FinishedLoading) {
        currentState = state
        onFinishedLoading(state).then {
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