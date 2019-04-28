package me.jameshunt.flow

import me.jameshunt.flow.promise.always
import me.jameshunt.flow.promise.catch
import java.lang.ref.WeakReference

internal object FlowManager {
    private var rootFlow: FlowController<*, Unit>? = null

    private lateinit var transientActivity: WeakReference<FlowActivity<*>>
    private val flowActivity: FlowActivity<*>
        get() = transientActivity.get() ?: throw IllegalStateException("Should never be null")

    internal val fragmentDisplayManager: FragmentDisplayManager
        get() = flowActivity.fragmentDisplayManager

    internal val rootViewManager: RootViewManager
        get() = flowActivity.rootViewManager

    private val shouldResume: Boolean
        get() = this.rootFlow != null

    fun launchFlow(flowActivity: FlowActivity<*>) {
        this.transientActivity = WeakReference(flowActivity)

        when (shouldResume) {
            true -> this.resumeActiveFlowControllers()
            false -> this.rootFlow = flowActivity.getInitialGroupFlow().also { rootFlow ->
                rootFlow
                    .launchFlow(flowActivity.getInitialArgs())
                    .catch { it.printStackTrace() }
                    .always {
                        this.rootFlow = null
                        this.flowActivity.onFlowFinished()
                        println("flow completed")
                    }
            }
        }
    }

    fun delegateBack() {
        rootFlow!!.handleBack()
    }

    private fun resumeActiveFlowControllers() {
        fragmentDisplayManager.removeAll(blocking = true)

        val flowGroup = (rootFlow!! as FragmentGroupFlowController<*, *>).findGroup()

        flowGroup.resume()

        flowGroup
            .childFlows
            .map { it as FragmentFlowController<*, *> }
            .map { it.getFragmentFlowLeaf() }
            .forEach { it.resume() }
    }

    private fun FragmentGroupFlowController<*, *>.findGroup(): FragmentGroupFlowController<*, *> {
        return this.childFlows
            .mapNotNull { it as? FragmentGroupFlowController<*,*> }
            .firstOrNull()
            ?.findGroup()

            ?: this.childFlows
                .mapNotNull { it.findGroup() }
                .firstOrNull()

            ?: this
    }

    private fun FlowController<*, *>.findGroup(): FragmentGroupFlowController<*, *>? {
        return this.childFlows
            .mapNotNull { it as? FragmentGroupFlowController<*, *> }
            .firstOrNull()
            ?.findGroup()

            ?: this.childFlows
                .firstOrNull()
                ?.findGroup()
    }

    //find group must be called before this
    private tailrec fun FragmentFlowController<*, *>.getFragmentFlowLeaf(): FragmentFlowController<*, *> {
        return when (this.childFlows.isEmpty()) {
            true -> this

            // fragmentFlowControllers will only ever have one child
            false -> (childFlows.first() as FragmentFlowController<*, *>).getFragmentFlowLeaf()
        }
    }

}