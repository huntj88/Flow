package me.jameshunt.flow

import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

internal object FlowManager: CoroutineScope by object: CoroutineScope{
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
} {

    private var rootFlow: AndroidFlowController<*, Unit>? = null

    private lateinit var transientActivity: WeakReference<FlowActivity<*>>
    private val flowActivity: FlowActivity<*>
        get() = transientActivity.get() ?: throw IllegalStateException("Should never be null")

    private val shouldResume: Boolean
        get() = this.rootFlow != null

    val fragmentDisplayManager: FragmentDisplayManager
        get() = flowActivity.fragmentDisplayManager

    val rootViewManager: RootViewManager
        get() = flowActivity.rootViewManager

    val activityForResultManager = ActivityForResultManager { flowActivity }

    fun launchFlow(flowActivity: FlowActivity<*>) {
        this.transientActivity = WeakReference(flowActivity)


            when (shouldResume) {
                true -> launch(Dispatchers.Main) { this@FlowManager.resumeActiveFlowControllers() }
                false -> this@FlowManager.rootFlow =
                    flowActivity.getInitialGroupFlow().also { rootFlow ->
                        launch(Dispatchers.Main) {
                            try {
                                rootFlow.launchFlow(flowActivity.getInitialArgs()).await()
                            } finally {
                                this@FlowManager.rootFlow = null
                                this@FlowManager.flowActivity.onFlowFinished()
                            }
                        }
                    }
        }
    }

    fun delegateBack() {
        (rootFlow as AndroidFlowController<*, *>).handleBack()
    }

    suspend fun resumeActiveFlowControllers() {
        fragmentDisplayManager.removeAll()

        val flowGroup = (rootFlow!! as FragmentGroupFlowController<*, *>).findGroup()

        flowGroup.resume()

        flowGroup
            .childFlows
            .map { it as FragmentFlowController<*, *> }
            .mapNotNull { it.getFragmentFlowLeaf() }
            .forEach { it.resume() }
    }

    fun retryUncommittedFragmentTransactions() {
        val flowGroup = (rootFlow!! as FragmentGroupFlowController<*, *>).findGroup()

        flowGroup
            .childFlows
            .map { it as FragmentFlowController<*, *> }
            .mapNotNull { it.getFragmentFlowLeaf() }
            .mapNotNull { it.uncommittedTransaction }
            .forEach { transaction -> transaction() }
    }

    private fun FragmentGroupFlowController<*, *>.findGroup(): FragmentGroupFlowController<*, *> {
        return this.childFlows
            .mapNotNull { it as? FragmentGroupFlowController<*, *> }
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

    // find group must be called before this
    private fun FragmentFlowController<*, *>.getFragmentFlowLeaf(): FragmentFlowController<*, *>? {
        return when (this.childFlows.isEmpty()) {
            true -> this

            // fragmentFlowControllers will only ever have one child
            false -> childFlows
                .mapNotNull { it as? FragmentFlowController<*, *> }
                .firstOrNull()
                ?.getFragmentFlowLeaf()
                ?: this
        }
    }
}
