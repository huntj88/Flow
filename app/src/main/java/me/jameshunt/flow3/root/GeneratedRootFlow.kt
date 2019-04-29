package me.jameshunt.flow3.root

import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.ViewId
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.then

abstract class GeneratedRootFlow(viewId: ViewId) : FragmentFlowController<Unit, Unit>(viewId) {

    protected sealed class RootFlowState: State {
        interface FromOne // can go from One to whichever states implemented this
        interface FromTwo
        interface FromThree
        interface FromFour

        object Back: RootFlowState(), BackState, FromTwo

        object One : RootFlowState(), FromFour
        data class Two(val arg: String) : RootFlowState(), FromThree
        data class Three(val arg: String) : RootFlowState(), FromOne
        data class Four(val arg: String) : RootFlowState(), FromOne, FromTwo
    }

    protected abstract fun onOne(state: RootFlowState.One): Promise<RootFlowState.FromOne>
    protected abstract fun onTwo(state: RootFlowState.Two): Promise<RootFlowState.FromTwo>
    protected abstract fun onThree(state: RootFlowState.Three): Promise<RootFlowState.FromThree>
    protected abstract fun onFour(state: RootFlowState.Four): Promise<RootFlowState.FromFour>

    final override fun onStart(state: InitialState<Unit>) {
        toOne(RootFlowState.One)
    }

    private fun toOne(state: RootFlowState.One) {
        currentState = state
        onOne(state).then {
            when(it) {
                is RootFlowState.Three -> toThree(it)
                is RootFlowState.Four -> toFour(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toTwo(state: RootFlowState.Two) {
        currentState = state
        onTwo(state).then {
            when(it) {
                is RootFlowState.Four -> toFour(it)
                is RootFlowState.Back -> it.onBack()
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toThree(state: RootFlowState.Three) {
        currentState = state
        onThree(state).then {
            when(it) {
                is RootFlowState.Two -> toTwo(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }

    private fun toFour(state: RootFlowState.Four) {
        currentState = state
        onFour(state).then {
            when(it) {
                is RootFlowState.One -> toOne(it)
                else -> throw IllegalStateException("Illegal transition from: $state, to: $it")
            }
        }
    }
}