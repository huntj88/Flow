package me.jameshunt.flow3.summary

import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.ViewId
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.then
import me.jameshunt.flow.proxy
import me.jameshunt.flow3.TestFragment
import me.jameshunt.flow3.portfolio.PortfolioFlowController


class SummaryFlowController(viewId: ViewId): GeneratedSummaryController(viewId) {

    private val testFragmentProxy = proxy(TestFragment::class.java)

    override fun onGatherData(state: SummaryFlowState.GatherData): Promise<SummaryFlowState.FromGatherData> {
        return Promise(SummaryFlowState.Render)
    }

    override fun onRender(state: SummaryFlowState.Render): Promise<SummaryFlowState.FromRender> {
        return this.flow(fragmentProxy = this.testFragmentProxy, input = "wow").forResult<Unit, SummaryFlowState.FromRender>(
            onBack = { TODO() },
            onComplete = {
                // wrapper in a Promise in case you need to do some async stuff
                Promise(SummaryFlowState.CryptoSelected)
            }
        )
    }

    override fun onCryptoSelected(state: SummaryFlowState.CryptoSelected): Promise<SummaryFlowState.FromCryptoSelected> {
        return this.flow(controller = PortfolioFlowController::class.java, input = Unit).forResult(
            onBack = { TODO() },
            onComplete = {
                TODO()
            }
        )
    }
}

abstract class GeneratedSummaryController(viewId: ViewId): FragmentFlowController<Unit, Unit>(viewId) {

    protected sealed class SummaryFlowState : State {
        interface FromGatherData
        interface FromRender
        interface FromCryptoSelected

        object Back : SummaryFlowState(), BackState

        object GatherData : SummaryFlowState()
        object Render : SummaryFlowState(), FromGatherData
        object CryptoSelected : SummaryFlowState(), FromRender
    }

    protected abstract fun onGatherData(state: SummaryFlowState.GatherData): Promise<SummaryFlowState.FromGatherData>
    protected abstract fun onRender(state: SummaryFlowState.Render): Promise<SummaryFlowState.FromRender>
    protected abstract fun onCryptoSelected(state: SummaryFlowState.CryptoSelected): Promise<SummaryFlowState.FromCryptoSelected>

    final override fun onStart(state: InitialState<Unit>) {
        toGatherData(SummaryFlowState.GatherData)
    }

    private fun toGatherData(state: SummaryFlowState.GatherData) {
        currentState = state
        onGatherData(state).then {
            when(it) {
                is SummaryFlowState.Render -> toRender(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toRender(state: SummaryFlowState.Render) {
        currentState = state
        onRender(state).then {
            when(it) {
                is SummaryFlowState.CryptoSelected-> toCryptoSelected(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toCryptoSelected(state: SummaryFlowState.CryptoSelected) {
        currentState = state
        onCryptoSelected(state).then {
//            when(it) {
////                is SummaryFlowState.Ba-> toThree(it)
//                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
//            }
            throw IllegalStateException("Illegal transition from: $state, to: $it")
        }
    }

    final override fun resume(currentState: State) {
        when (currentState) {
            is InitialState<*> -> this.onStart(currentState as InitialState<Unit>)
            is SummaryFlowState -> currentState.resumeState()
            else -> throw IllegalStateException("State is not part of this flow")
        }
    }

    private fun SummaryFlowState.resumeState() {
        when(this) {
            is SummaryFlowState.GatherData -> this@GeneratedSummaryController.onGatherData(this)
            is SummaryFlowState.Render -> this@GeneratedSummaryController.onRender(this)
            is SummaryFlowState.CryptoSelected -> this@GeneratedSummaryController.onCryptoSelected(this)
        }
    }
}