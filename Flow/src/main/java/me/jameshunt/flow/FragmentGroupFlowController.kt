package me.jameshunt.flow

import me.jameshunt.flow.promise.always

open class FragmentGroupFlowController(internal val layoutId: LayoutId): FlowController<FragmentGroupFlowController.FlowsInGroup, Unit>() {

    data class FlowsInGroup(val map: Map<ViewId, Class<FragmentFlowController<Unit, Unit>>>)

    override fun onStart(state: InitialState<FlowsInGroup>) {
        FlowManager.rootViewManager.get()!!.setNewRoot(layoutId)

        state.arg.map.forEach { (viewId, flowController) ->
            this.flow(controller = flowController, viewId = viewId, arg = Unit).always {
                this.onBack()
            }
        }
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