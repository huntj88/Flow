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

    fun launchFlow(flowActivity: FlowActivity) {
        this.flowActivity = WeakReference(flowActivity)

        when (FlowManager.shouldResume) {
            true -> this.resumeLeafFlowControllers()
            false -> FlowManager.launchFlow(
                getInitialFlow = flowActivity::getInitialFlow,
                onFlowFinished = { this.flowActivity.get()!!.onFlowFinished() }
            )
        }
    }

    private fun resumeLeafFlowControllers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}