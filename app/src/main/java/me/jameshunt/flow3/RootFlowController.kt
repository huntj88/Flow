package me.jameshunt.flow3

import android.util.Log
import me.jameshunt.flow.always
import me.jameshunt.flowandroid.FragmentFlowController
import me.jameshunt.flowandroid.FragmentProxy
import me.jameshunt.flowandroid.ViewId

class RootFlowController(viewId: ViewId): GeneratedRootFlow<Unit, Unit>(viewId) {

    private val testFragmentProxy = FragmentProxy(TestFragment::class.java)

    override fun onStart(state: InitialState<Unit>) {
        Log.d("root","start")
        state.toDeepLink("hello")
    }

    override fun onDeepLink(state: DeepLink) {
        Log.d("root",state.arg)
        this.flow(fragmentProxy = testFragmentProxy, args = Unit).always {
            Log.d("root","fragment resolved")
        }
    }
}

abstract class GeneratedRootFlow<Input, Output>(viewId: ViewId): FragmentFlowController<Input, Output>(viewId) {

    data class DeepLink(val arg: String): State

    abstract fun onDeepLink(state: DeepLink)

    fun InitialState<Input>.toDeepLink(arg: String) {
        this.transition(to = DeepLink(arg)) {
            this@GeneratedRootFlow.onDeepLink(it)
        }
    }

    override fun resume(currentState: State) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}