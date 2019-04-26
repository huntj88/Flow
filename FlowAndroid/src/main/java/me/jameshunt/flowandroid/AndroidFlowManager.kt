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

    private val flowManager = FlowManager()

    private lateinit var flowActivity: WeakReference<FlowActivity>
    internal lateinit var rootViewManager: WeakReference<RootViewManager>
    internal lateinit var fragmentDisplayManager: WeakReference<FragmentDisplayManager>

    fun launchFlow(flowActivity: FlowActivity) {
        this.flowActivity = WeakReference(flowActivity)

        when (flowManager.shouldResume) {
            true -> this.resumeActiveFlowControllers()
            false -> flowManager.launchFlow(
                getInitialFlow = flowActivity::getInitialFlow,
                args = flowActivity.getInitialArgs(),
                onFlowFinished = { this.flowActivity.get()!!.onFlowFinished() }
            )
        }
    }

    fun delegateBack() {
        flowManager.delegateBack()
    }

    private fun resumeActiveFlowControllers() {
        rootViewManager.get()!!.setNewRoot(getGroupLayout())
    }

    private fun getGroupLayout(): LayoutId = (flowManager._rootFlow!! as FragmentGroupFlowController)
        .findGroup()
        .layoutId

    private fun FragmentGroupFlowController.findGroup(): FragmentGroupFlowController = this.childFlows
        .mapNotNull { it as? FragmentGroupFlowController }
        .firstOrNull()
        ?.findGroup()
        ?: this

}