package me.jameshunt.flow3.root

import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.ViewId
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.then

abstract class GeneratedRootFlow(viewId: ViewId) : FragmentFlowController<Unit, Unit>(viewId) {

    sealed class RootFlowState: State {
        interface FromOne // can go from One to whichever states implemented this
        interface FromTwo
        interface FromThree
        interface FromFour

        object One : RootFlowState(), FromFour
        data class Two(val arg: String) : RootFlowState(), FromThree
        data class Three(val arg: String) : RootFlowState(), FromOne
        data class Four(val arg: String) : RootFlowState(), FromOne, FromTwo
    }

    abstract fun onOne(state: RootFlowState.One): Promise<RootFlowState.FromOne>
    abstract fun onTwo(state: RootFlowState.Two): Promise<RootFlowState.FromTwo>
    abstract fun onThree(state: RootFlowState.Three): Promise<RootFlowState.FromThree>
    abstract fun onFour(state: RootFlowState.Four): Promise<RootFlowState.FromFour>

    override fun onStart(state: InitialState<Unit>) {
        toOne(RootFlowState.One)
    }

    private fun toOne(state: RootFlowState.One) {
        currentState = state
        onOne(state).then {
            when(it) {
                is RootFlowState.Three -> toThree(it)
                is RootFlowState.Four -> toFour(it)
                else -> throw IllegalStateException("Illegal transition")
            }
        }
    }

    private fun toTwo(state: RootFlowState.Two) {
        currentState = state
        onTwo(state).then {
            when(it) {
                is RootFlowState.Four -> toFour(it)
                else -> throw IllegalStateException("Illegal transition")
            }
        }
    }

    private fun toThree(state: RootFlowState.Three) {
        currentState = state
        onThree(state).then {
            when(it) {
                is RootFlowState.Two -> toTwo(it)
                else -> throw IllegalStateException("Illegal transition")
            }
        }
    }

    private fun toFour(state: RootFlowState.Four) {
        currentState = state
        onFour(state).then {
            when(it) {
                is RootFlowState.One -> toOne(it)
                else -> throw IllegalStateException("Illegal transition")
            }
        }
    }

//    fun InitialState<Input>.toDeepLink(arg: String) {
//        this.transition(to = RootFlowState.One(arg)) {
//            this@GeneratedRootFlow.onOne(it)
//        }
//    }
//
//    fun RootFlowState.One.toDeepLink2(arg: String) {
//        this.transition(to = RootFlowState.Two(arg)) {
//            this@GeneratedRootFlow.onTwo(it)
//        }
//    }
//
//    fun RootFlowState.One.toBack() {
//        this@GeneratedRootFlow.onBack()
//    }
//
//    fun RootFlowState.Two.toBack() {
//        this@GeneratedRootFlow.onBack()
//    }

    override fun resume(currentState: State) {
        when (currentState) {
            is InitialState<*> -> this.onStart(currentState as InitialState<Unit>)
            is RootFlowState -> currentState.resumeState()
            else -> throw IllegalStateException("State is not part of this flow")
        }
    }

    private fun RootFlowState.resumeState() {
        when(this) {
            is RootFlowState.One -> this@GeneratedRootFlow.onOne(this)
            is RootFlowState.Two -> this@GeneratedRootFlow.onTwo(this)
        }
    }

}