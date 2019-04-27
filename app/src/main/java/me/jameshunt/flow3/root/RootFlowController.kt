package me.jameshunt.flow3.root

import android.util.Log
import me.jameshunt.flow.*
import me.jameshunt.flow3.R
import me.jameshunt.flow3.TestFragment
import me.jameshunt.flow3.ViewPagerGroupController

class RootFlowController(viewId: ViewId) : GeneratedRootFlow<Unit, Unit>(viewId) {

    private val testFragmentProxy = proxy(TestFragment::class.java)
//    private val testFragmentProxy2 = proxy(TestFragment::class.java)

    override fun onStart(state: InitialState<Unit>) {
        Log.d("root", "start")
        state.toDeepLink("hello")
    }

    override fun onDeepLink(state: RootFlowState.DeepLink) {
        Log.d("root", state.arg)
        this.flow(fragmentProxy = testFragmentProxy, arg = state.arg)
            .complete { state.toDeepLink2("wooow") }
            .back {
                Log.d("root", "back")
                state.toBack()
            }
    }

    override fun onDeepLink2(state: RootFlowState.DeepLink2) {
//        Log.d("root", state.arg)
//        this.flow(fragmentProxy = testFragmentProxy2, arg = state.arg)
//            .complete { Log.d("root", "fragment resolved") }
//            .back { state.toBack() }

        val groupArgs = FragmentGroupFlowController.FlowsInGroup(
            mapOf(
                RootFlowController::class.java.putInView(R.id.groupPagerZero),
                RootFlowController::class.java.putInView(R.id.groupPagerOne),
                RootFlowController::class.java.putInView(R.id.groupPagerTwo)
            ),
            Unit
        )

        this.flowGroup(controller = ViewPagerGroupController::class.java, arg = groupArgs)
            .back { state.toBack() }

//        this.flow(controller = RootFlowController::class.java,arg = Unit)
//            .back { state.toBack() }
    }
}

