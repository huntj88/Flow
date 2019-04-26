package me.jameshunt.flow3

import android.util.Log
import me.jameshunt.flow.*

class RootFlowController(viewId: ViewId) : GeneratedRootFlow<Unit, Unit>(viewId) {

    private val testFragmentProxy = proxy(TestFragment::class.java)
//    private val testFragmentProxy2 = proxy(TestFragment::class.java)

    override fun onStart(state: InitialState<Unit>) {
        Log.d("root", "start")
        state.toDeepLink("hello")
    }

    override fun onDeepLink(state: DeepLink) {
        Log.d("root", state.arg)
        this.flow(fragmentProxy = testFragmentProxy, arg = state.arg)
            .complete { state.toDeepLink2("wooow") }
            .back {
                Log.d("root", "back")
                state.toBack()
            }
    }

    override fun onDeepLink2(state: DeepLink2) {
//        Log.d("root", state.arg)
//        this.flow(fragmentProxy = testFragmentProxy2, arg = state.arg)
//            .complete { Log.d("root", "fragment resolved") }
//            .back { state.toBack() }

        val groupArgs = FragmentGroupFlowController.FlowsInGroup(
            mapOf(
                R.id.groupPagerZero.toPair(RootFlowController::class.java),
                R.id.groupPagerOne.toPair(RootFlowController::class.java),
                R.id.groupPagerTwo.toPair(RootFlowController::class.java)
            ),
            Unit
        )

        this.flowGroup(controller = ViewPagerGroupController::class.java, arg = groupArgs).back {
            state.toBack()
        }
    }
}

abstract class GeneratedRootFlow<Input, Output>(viewId: ViewId) : FragmentFlowController<Input, Output>(viewId) {

    data class DeepLink(val arg: String) : State
    data class DeepLink2(val arg: String) : State

    abstract fun onDeepLink(state: DeepLink)
    abstract fun onDeepLink2(state: DeepLink2)

    fun InitialState<Input>.toDeepLink(arg: String) {
        this.transition(to = DeepLink(arg)) {
            this@GeneratedRootFlow.onDeepLink(it)
        }
    }

    fun DeepLink.toDeepLink2(arg: String) {
        this.transition(to = DeepLink2(arg)) {
            this@GeneratedRootFlow.onDeepLink2(it)
        }
    }

    fun DeepLink.toBack() {
        this@GeneratedRootFlow.onBack()
    }

    fun DeepLink2.toBack() {
        this@GeneratedRootFlow.onBack()
    }

    override fun resume(currentState: State) {
        when (currentState) {
            is InitialState<*> -> this.onStart(currentState as InitialState<Input>)
            is DeepLink -> this.onDeepLink(currentState)
            is DeepLink2 -> this.onDeepLink2(currentState)
            else -> throw IllegalStateException("State is not part of this flow")
        }
    }

}