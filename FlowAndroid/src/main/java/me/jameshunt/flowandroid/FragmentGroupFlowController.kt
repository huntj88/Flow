package me.jameshunt.flowandroid

import me.jameshunt.flow.FlowController
import me.jameshunt.flow.Promise

class FragmentGroupFlowController(private val layoutId: LayoutId): FlowController<FragmentGroupFlowController.FlowsInGroup, Unit>() {

    data class FlowsInGroup(val map: Map<ViewId, Class<FragmentFlowController<Unit, Unit>>>)

    override fun resume(currentState: State) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStart(state: InitialState<FlowsInGroup>) {
        AndroidFlowManager.rootViewManager.get()!!.setNewRoot(layoutId)

        state.arg.map.forEach { (viewId, flowController) ->
            this.flow(controller = flowController, viewId = viewId, arg = Unit)
        }
    }

    // duplicated from FragmentFlowController
    private fun <NewInput, NewOutput> flow(
        controller: Class<FragmentFlowController<NewInput, NewOutput>>,
        viewId: ViewId,
        arg: NewInput
    ): Promise<NewOutput> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(arg)
    }
}