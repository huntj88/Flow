package me.jameshunt.flow

import me.jameshunt.flow.promise.*

abstract class FragmentFlowController<Input, Output>(private val viewId: ViewId) : FlowController<Input, Output>() {

    interface DoneState<Output> {
        val output: Output
    }

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

    fun <GroupInput, GroupOutput, Controller> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): Promise<FlowResult<GroupOutput>>
            where GroupInput : FragmentGroupFlowController.GroupInput,
                  Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {

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

    fun <NewInput, NewOutput, Controller : FragmentFlowController<NewInput, NewOutput>> flow(
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

    final override fun resume(currentState: State) {
        (activeFragment as FragmentProxy<Any?, Any?, FlowFragment<Any?, Any?>>?)?.let {
            FlowManager.fragmentDisplayManager.show(
                fragmentProxy = it,
                viewId = this.viewId
            )
        }
    }

    fun DoneState<Output>.onDone() {
        FlowManager.fragmentDisplayManager.remove(activeFragment)
        super.onDone(output)
    }

    final override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows.firstOrNull()?.handleBack() ?: this.activeFragment?.onBack()
    }
}
