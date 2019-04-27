package me.jameshunt.flow

import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.always

abstract class FragmentFlowController<Input, Output>(private val viewId: ViewId) : FlowController<Input, Output>() {

    private var activeFragment: FragmentProxy<*, *, *>? = null

    fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        arg: FragInput
    ): Promise<FlowResult<FragOutput>> {
        val displayManager = FlowManager.fragmentDisplayManager.get()
            ?: throw IllegalStateException("Should never happen")

        return displayManager
            .show(fragmentProxy = fragmentProxy, viewId = this.viewId)
            .also { this.activeFragment = fragmentProxy }
            .flowForResult(arg)
            .always { this.activeFragment = null }
    }

    fun <GroupInput, Controller: FragmentGroupFlowController<GroupInput>> flowGroup(
        controller: Class<Controller>,
        arg: FragmentGroupFlowController.FlowsInGroup<GroupInput>
    ): Promise<FlowResult<Unit>> {

        // remove all the fragments from this flowController before starting the next FlowController
        // (state will still be saved when they get back)
        // The fragments parent views could potentially no longer exist
        FlowManager.fragmentDisplayManager.get()!!.removeAll()

        val flowController = controller.newInstance()

        childFlows.add(flowController)

        return flowController.launchFlow(arg).always {
            childFlows.remove(flowController)
        }
    }

    fun <NewInput, NewOutput, Controller: FragmentFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        arg: NewInput
    ): Promise<FlowResult<NewOutput>> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(this@FragmentFlowController.viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(arg).always {
            childFlows.remove(flowController)
        }
    }

    override fun onDone(arg: Output) {
        val displayManager = FlowManager.fragmentDisplayManager.get()
            ?: throw IllegalStateException("Should never happen")

        displayManager.remove(activeFragment)
        super.onDone(arg)
    }

    override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows.firstOrNull()?.handleBack() ?: this.activeFragment?.onBack()
    }
}
