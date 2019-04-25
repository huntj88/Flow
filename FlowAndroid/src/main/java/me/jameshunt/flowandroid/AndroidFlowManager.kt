package me.jameshunt.flowandroid

import android.os.Handler
import android.os.Looper
import me.jameshunt.flow.*
import java.lang.ref.WeakReference
import java.util.concurrent.Executor

object AndroidFlowManager {

    init {
        val executor = Executor { command -> Handler(Looper.getMainLooper()).post(command) }
        PromiseDispatch.setMainExecutor(executor)
    }

    private lateinit var flowActivity: WeakReference<FlowActivity>
    internal lateinit var rootViewManager: WeakReference<RootViewManager>
    internal lateinit var fragmentDisplayManager: WeakReference<FragmentDisplayManager>

    fun launchFlow(flowActivity: FlowActivity) {
        this.flowActivity = WeakReference(flowActivity)

        when (FlowManager.shouldResume) {
            true -> this.resumeActiveFlowControllers()
            false -> FlowManager.launchFlow(
                getInitialFlow = flowActivity::getInitialFlow,
                args = flowActivity.getInitialArgs(),
                onFlowFinished = { this.flowActivity.get()!!.onFlowFinished() }
            )
        }
    }

    private fun resumeActiveFlowControllers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}