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
        fragmentDisplayManager.get()!!.removeAll()

        val flowGroup = (flowManager._rootFlow!! as FragmentGroupFlowController)
            .findGroup()

        rootViewManager.get()!!.setNewRoot(flowGroup.layoutId)

        flowGroup
            .childFlows
            .map { it as FragmentFlowController<*,*> }
            .map { it.getFragmentFlowLeaf() }
            .forEach { it.resume() }
    }

    private fun FragmentGroupFlowController.findGroup(): FragmentGroupFlowController = this.childFlows
        .mapNotNull { it as? FragmentGroupFlowController }
        .firstOrNull()
        ?.findGroup()
        ?: this

    //find group must be called before this
    private fun FragmentFlowController<*,*>.getFragmentFlowLeaf(): FragmentFlowController<*,*> {
        return when(this.childFlows.isEmpty()) {
            true -> this

            // fragmentFlowControllers will only ever have one child
            false -> (childFlows.first() as FragmentFlowController<*, *>).getFragmentFlowLeaf()
        }
    }

}