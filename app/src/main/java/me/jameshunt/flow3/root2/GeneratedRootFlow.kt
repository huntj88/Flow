package me.jameshunt.flow3.root2

import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.ViewId
import me.jameshunt.flow.promise.Promise

abstract class GeneratedRootFlow<Input, Output>(viewId: ViewId) : FragmentFlowController<Input, Output>(viewId) {

    sealed class RootFlowState: State {
        interface FromOne // can go from One to whichever states implemented this
        interface FromTwo
        interface FromThree
        interface FromFour

        data class One(val arg: String) : RootFlowState(), FromFour
        data class Two(val arg: String) : RootFlowState(), FromThree
        data class Three(val arg: String) : RootFlowState(), FromOne
        data class Four(val arg: String) : RootFlowState(), FromOne, FromTwo
    }

    abstract fun onOne(state: RootFlowState.One): Promise<RootFlowState.FromOne>
    abstract fun onTwo(state: RootFlowState.Two): Promise<RootFlowState.FromTwo>
    abstract fun onThree(state: RootFlowState.Three): Promise<RootFlowState.FromThree>
    abstract fun onFour(state: RootFlowState.Four): Promise<RootFlowState.FromFour>

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
            is InitialState<*> -> this.onStart(currentState as InitialState<Input>)
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