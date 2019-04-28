package me.jameshunt.flow3.portfolio

import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.ViewId
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.then
import me.jameshunt.flow.proxy
import me.jameshunt.flow3.TestFragment


class PortfolioFlowController(viewId: ViewId): GeneratedPortfolioController(viewId) {

    private val testFragmentProxy = proxy(TestFragment::class.java)

    override fun onGatherData(state: PortfolioFlowState.GatherData): Promise<PortfolioFlowState.FromGatherData> {
        return Promise(PortfolioFlowState.Render)
    }

    override fun onRender(state: PortfolioFlowState.Render): Promise<PortfolioFlowState.FromRender> {
        return Promise(PortfolioFlowState.Transactions)
    }

    override fun onTransactions(state: PortfolioFlowState.Transactions): Promise<PortfolioFlowState.FromTransactions> {
        return this.flow(fragmentProxy = testFragmentProxy, input = "wooooow").forResult<Unit, PortfolioFlowState.FromTransactions>(
            onBack = { Promise(PortfolioFlowState.Render) },
            onComplete = { Promise(PortfolioFlowState.Render) }
        )
    }
}

abstract class GeneratedPortfolioController(viewId: ViewId): FragmentFlowController<Unit, Unit>(viewId) {

    protected sealed class PortfolioFlowState : State {
        interface FromGatherData
        interface FromRender
        interface FromTransactions

        object Back : PortfolioFlowState(), BackState

        object GatherData : PortfolioFlowState()
        object Render : PortfolioFlowState(), FromGatherData, FromTransactions
        object Transactions : PortfolioFlowState(), FromRender
    }

    protected abstract fun onGatherData(state: PortfolioFlowState.GatherData): Promise<PortfolioFlowState.FromGatherData>
    protected abstract fun onRender(state: PortfolioFlowState.Render): Promise<PortfolioFlowState.FromRender>
    protected abstract fun onTransactions(state: PortfolioFlowState.Transactions): Promise<PortfolioFlowState.FromTransactions>

    final override fun onStart(state: InitialState<Unit>) {
        toGatherData(PortfolioFlowState.GatherData)
    }

    private fun toGatherData(state: PortfolioFlowState.GatherData) {
        currentState = state
        onGatherData(state).then {
            when(it) {
                is PortfolioFlowState.Render -> toRender(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toRender(state: PortfolioFlowState.Render) {
        currentState = state
        onRender(state).then {
            when(it) {
                is PortfolioFlowState.Transactions -> toTransactions(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toTransactions(state: PortfolioFlowState.Transactions) {
        currentState = state
        onTransactions(state).then {
            when(it) {
                is PortfolioFlowState.Render -> toRender(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    final override fun resume(currentState: State) {
        when (currentState) {
            is InitialState<*> -> this.onStart(currentState as InitialState<Unit>)
            is PortfolioFlowState -> currentState.resumeState()
            else -> throw IllegalStateException("State is not part of this flow")
        }
    }

    private fun PortfolioFlowState.resumeState() {
        when(this) {
            is PortfolioFlowState.GatherData -> this@GeneratedPortfolioController.onGatherData(this)
            is PortfolioFlowState.Render -> this@GeneratedPortfolioController.onRender(this)
            is PortfolioFlowState.Transactions -> this@GeneratedPortfolioController.onTransactions(this)
        }
    }
}