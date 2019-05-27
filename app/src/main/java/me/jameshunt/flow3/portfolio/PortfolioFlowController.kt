package me.jameshunt.flow3.portfolio

import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.map
import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.proxy
import me.jameshunt.flow3.TestFragment


class PortfolioFlowController: GeneratedPortfolioController() {

    private val testFragmentProxy = proxy(TestFragment::class.java)

    override fun onGatherData(state: PortfolioFlowState.GatherData): Promise<PortfolioFlowState.FromGatherData> {
        return Promise.value(PortfolioFlowState.Render)
    }

    override fun onRender(state: PortfolioFlowState.Render): Promise<PortfolioFlowState.FromRender> {
        return Promise.value(PortfolioFlowState.Transactions)
    }

    override fun onTransactions(state: PortfolioFlowState.Transactions): Promise<PortfolioFlowState.FromTransactions> {
        return this.flow(fragmentProxy = testFragmentProxy, input = "wooooow").forResult<Unit, PortfolioFlowState.FromTransactions>(
            onBack = { Promise.value(PortfolioFlowState.Render) },
            onComplete = { Promise.value(PortfolioFlowState.Render) }
        )
    }
}

abstract class GeneratedPortfolioController: FragmentFlowController<Unit, Unit>() {

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
        onGatherData(state).map {
            when(it) {
                is PortfolioFlowState.Render -> toRender(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toRender(state: PortfolioFlowState.Render) {
        currentState = state
        onRender(state).map {
            when(it) {
                is PortfolioFlowState.Transactions -> toTransactions(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toTransactions(state: PortfolioFlowState.Transactions) {
        currentState = state
        onTransactions(state).map {
            when(it) {
                is PortfolioFlowState.Render -> toRender(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }
}