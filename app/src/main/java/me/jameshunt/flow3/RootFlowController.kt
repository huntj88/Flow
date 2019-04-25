package me.jameshunt.flow3

import android.util.Log
import me.jameshunt.flow.always
import me.jameshunt.flowandroid.FragmentFlowController
import me.jameshunt.flowandroid.FragmentProxy
import me.jameshunt.flowandroid.ViewId

class RootFlowController(viewId: ViewId): GeneratedRootFlow<Unit, Unit>(viewId) {

    private val testFragmentProxy = FragmentProxy(TestFragment::class.java)
    private val testFragmentProxy2 = FragmentProxy(TestFragment::class.java)

    override fun onStart(state: InitialState<Unit>) {
        Log.d("root","start")
        state.toDeepLink("hello")
    }

    override fun onDeepLink(state: DeepLink) {
        Log.d("root",state.arg)
        this.flow(fragmentProxy = testFragmentProxy, arg = state.arg).always {
            state.toDeepLink2("wooow")
        }
    }

    override fun onDeepLink2(state: DeepLink2) {
        Log.d("root",state.arg)
        this.flow(fragmentProxy = testFragmentProxy2, arg = state.arg).always {
            Log.d("root","fragment resolved")
        }
    }
}

abstract class GeneratedRootFlow<Input, Output>(viewId: ViewId): FragmentFlowController<Input, Output>(viewId) {

    data class DeepLink(val arg: String): State
    data class DeepLink2(val arg: String): State

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

    override fun resume(currentState: State) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}