package me.jameshunt.flow3.root

import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.ViewId

abstract class GeneratedRootFlow<Input, Output>(viewId: ViewId) : FragmentFlowController<Input, Output>(viewId) {

    sealed class RootFlowState: State {
        data class DeepLink(val arg: String) : RootFlowState()
        data class DeepLink2(val arg: String) : RootFlowState()
    }

    abstract fun onDeepLink(state: RootFlowState.DeepLink)
    abstract fun onDeepLink2(state: RootFlowState.DeepLink2)

    fun InitialState<Input>.toDeepLink(arg: String) {
        this.transition(to = RootFlowState.DeepLink(arg)) {
            this@GeneratedRootFlow.onDeepLink(it)
        }
    }

    fun RootFlowState.DeepLink.toDeepLink2(arg: String) {
        this.transition(to = RootFlowState.DeepLink2(arg)) {
            this@GeneratedRootFlow.onDeepLink2(it)
        }
    }

    fun RootFlowState.DeepLink.toBack() {
        this@GeneratedRootFlow.onBack()
    }

    fun RootFlowState.DeepLink2.toBack() {
        this@GeneratedRootFlow.onBack()
    }

    override fun resume(currentState: State) {
        when (currentState) {
            is InitialState<*> -> this.onStart(currentState as InitialState<Input>)
            is RootFlowState -> currentState.resumeState()
            else -> throw IllegalStateException("State is not part of this flow")
        }
    }

    private fun RootFlowState.resumeState() {
        when(this) {
            is RootFlowState.DeepLink -> this@GeneratedRootFlow.onDeepLink(this)
            is RootFlowState.DeepLink2 -> this@GeneratedRootFlow.onDeepLink2(this)
        }
    }

}