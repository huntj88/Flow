package me.jameshunt.flow3.summary

import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.map
import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.proxy
import me.jameshunt.flow3.TestFragment
import me.jameshunt.flow3.portfolio.PortfolioFlowController
import me.jameshunt.flow3.summary.GeneratedSummaryController.SummaryFlowState.*

data class SummaryInput(
    val string: String
)

class SummaryFlowController: GeneratedSummaryController() {

    private val testFragmentProxy = proxy(TestFragment::class.java)

    override fun onGatherData(state: GatherData): Promise<FromGatherData> {
        return Promise.value(Render)
    }

    override fun onRender(state: Render): Promise<FromRender> {
        return this.flow(fragmentProxy = this.testFragmentProxy, input = "wow").forResult<Unit, FromRender>(
            onComplete = {
                // wrapper in a Promise in case you need to do some async stuff
                Promise.value(CryptoSelected)
            }
        )
    }

    override fun onCryptoSelected(state: CryptoSelected): Promise<FromCryptoSelected> {
        return this.flow(controller = PortfolioFlowController::class.java, input = Unit).forResult(
            onBack = { TODO() },
            onComplete = {
                TODO()
            }
        )
    }
}

abstract class GeneratedSummaryController: FragmentFlowController<Unit, Unit>() {

    protected sealed class SummaryFlowState : State {
        interface FromGatherData
        interface FromRender
        interface FromCryptoSelected

        object Back : SummaryFlowState(), BackState

        object GatherData : SummaryFlowState()
        object Render : SummaryFlowState(), FromGatherData
        object CryptoSelected : SummaryFlowState(), FromRender
    }

    protected abstract fun onGatherData(state: GatherData): Promise<FromGatherData>
    protected abstract fun onRender(state: Render): Promise<FromRender>
    protected abstract fun onCryptoSelected(state: CryptoSelected): Promise<FromCryptoSelected>

    final override fun onStart(state: InitialState<Unit>) {
        toGatherData(GatherData)
    }

    private fun toGatherData(state: GatherData) {
        currentState = state
        onGatherData(state).map {
            when(it) {
                is Render -> toRender(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toRender(state: Render) {
        currentState = state
        onRender(state).map {
            when(it) {
                is CryptoSelected-> toCryptoSelected(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toCryptoSelected(state: CryptoSelected) {
        currentState = state
        onCryptoSelected(state).map {
//            when(it) {
////                is SummaryFlowState.Ba-> toThree(it)
//                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
//            }
            throw IllegalStateException("Illegal transition from: $state, to: $it")
        }
    }
}