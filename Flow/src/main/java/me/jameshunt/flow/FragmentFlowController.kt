package me.jameshunt.flow

import me.jameshunt.flow.promise.*

abstract class FragmentFlowController<Input, Output>(private val viewId: ViewId) : FlowController<Input, Output>() {

    private var activeFragment: FragmentProxy<*, *, *>? = null

    fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): Promise<FlowResult<FragOutput>> {

        return FlowManager.fragmentDisplayManager
            .show(fragmentProxy = fragmentProxy, viewId = this.viewId)
            .also { this.activeFragment = fragmentProxy }
            .flowForResult(input)
            .always { this.activeFragment = null }
    }

    fun <GroupInput, Controller: FragmentGroupFlowController<GroupInput>> flowGroup(
        controller: Class<Controller>,
        input: FragmentGroupFlowController.FlowsInGroup<GroupInput>
    ): Promise<FlowResult<Unit>> {

        // remove all the fragments from this flowController before starting the next FlowController
        // (state will still be saved when they get back)
        // The fragments parent views could potentially no longer exist
        FlowManager.fragmentDisplayManager.removeAll()

        val flowController = controller.newInstance()

        childFlows.add(flowController)

        return flowController.launchFlow(input).always {
            childFlows.remove(flowController)
        }
    }

    fun <NewInput, NewOutput, Controller: FragmentFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(this@FragmentFlowController.viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(input).always {
            childFlows.remove(flowController)
        }
    }

    override fun onDone(output: Output) {
        FlowManager.fragmentDisplayManager.remove(activeFragment)
        super.onDone(output)
    }

    override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows.firstOrNull()?.handleBack() ?: this.activeFragment?.onBack()
    }
}
