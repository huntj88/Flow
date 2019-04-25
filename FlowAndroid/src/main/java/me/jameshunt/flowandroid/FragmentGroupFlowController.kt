package me.jameshunt.flowandroid

import me.jameshunt.flow.*

open class FragmentGroupFlowController(private val layoutId: LayoutId): FlowController<FragmentGroupFlowController.FlowsInGroup, Unit>() {

    data class FlowsInGroup(val map: Map<ViewId, Class<FragmentFlowController<Unit, Unit>>>)

    override fun onStart(state: InitialState<FlowsInGroup>) {
        AndroidFlowManager.rootViewManager.get()!!.setNewRoot(layoutId)

        state.arg.map.forEach { (viewId, flowController) ->
            this.flow(controller = flowController, viewId = viewId, arg = Unit).always {
                this.onBack()
            }
        }
    }

    // duplicated from FragmentFlowController
    private fun <NewInput, NewOutput> flow(
        controller: Class<FragmentFlowController<NewInput, NewOutput>>,
        viewId: ViewId,
        arg: NewInput
    ): Promise<FlowResult<NewOutput>> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(arg)
    }

    override fun resume(currentState: State) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    protected open fun childIndexToDelegateBack(): Int = 0

    override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows[childIndexToDelegateBack()].handleBack()
    }
}