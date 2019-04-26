package me.jameshunt.flow

import me.jameshunt.flow.promise.always
import me.jameshunt.flow.promise.catch
import java.lang.ref.WeakReference

object FlowManager {

    private var rootFlow: FlowController<*, Unit>? = null

    private val shouldResume: Boolean
        get() = this.rootFlow != null


    private lateinit var flowActivity: WeakReference<FlowActivity<*>>
    internal lateinit var rootViewManager: WeakReference<RootViewManager>
    lateinit var fragmentDisplayManager: WeakReference<FragmentDisplayManager>

    fun launchFlow(flowActivity: FlowActivity<*>) {
        this.flowActivity = WeakReference(flowActivity)

        when (shouldResume) {
            true -> this.resumeActiveFlowControllers()
            false -> this.rootFlow = flowActivity.getInitialGroupFlow().also { rootFlow ->
                rootFlow
                    .launchFlow(flowActivity.getInitialArgs())
                    .catch { it.printStackTrace() }
                    .always {
                        this.rootFlow = null
                        this.flowActivity.get()!!.onFlowFinished()
                        println("flow completed")
                    }
            }
        }
    }

    fun delegateBack() {
        rootFlow?.handleBack()
    }

    private fun resumeActiveFlowControllers() {
        fragmentDisplayManager.get()!!.removeAll(blocking = true)

        val flowGroup = (rootFlow!! as FragmentGroupFlowController<*>).findGroup()

        rootViewManager.get()!!.setNewRoot(flowGroup.layoutId)

        flowGroup.resume()

        flowGroup
            .childFlows
            .map { it as FragmentFlowController<*, *> }
            .map { it.getFragmentFlowLeaf() }
            .forEach { it.resume() }
    }

    private fun FragmentGroupFlowController<*>.findGroup(): FragmentGroupFlowController<*> {
        return this.childFlows
            .mapNotNull { it as? FragmentGroupFlowController<*> }
            .firstOrNull()
            ?.findGroup()

            ?: this.childFlows
                .mapNotNull { it.findGroup() }
                .firstOrNull()

            ?: this
    }

    private fun FlowController<*,*>.findGroup(): FragmentGroupFlowController<*>? {
        return this.childFlows
            .mapNotNull { it as? FragmentGroupFlowController<*> }
            .firstOrNull()
            ?.findGroup()

            ?: this.childFlows
                .firstOrNull()
                ?.findGroup()
    }

    //find group must be called before this
    private fun FragmentFlowController<*, *>.getFragmentFlowLeaf(): FragmentFlowController<*, *> {
        return when (this.childFlows.isEmpty()) {
            true -> this

            // fragmentFlowControllers will only ever have one child
            false -> (childFlows.first() as FragmentFlowController<*, *>).getFragmentFlowLeaf()
        }
    }

}