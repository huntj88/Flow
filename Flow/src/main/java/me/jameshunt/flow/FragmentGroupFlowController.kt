package me.jameshunt.flow

import android.view.ViewGroup

abstract class FragmentGroupFlowController<T>(internal val layoutId: LayoutId): FlowController<FragmentGroupFlowController.FlowsInGroup<T>, Unit>() {

    data class FlowsInGroup<T>(
        val map: Map<ViewId, Class<FragmentFlowController<Unit, Unit>>>,
        val extra: T
    )

    override fun onStart(state: InitialState<FlowsInGroup<T>>) {
        val layout = FlowManager.rootViewManager.get()!!.setNewRoot(layoutId)
        setupGroup(layout, state.arg)
    }

    abstract fun setupGroup(layout: ViewGroup, flowsInGroup: FlowsInGroup<T>)

    protected open fun childIndexToDelegateBack(): Int = 0

    override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows[childIndexToDelegateBack()].handleBack()
    }

    override fun resume(currentState: State) {
        this.onStart(currentState as InitialState<FlowsInGroup<T>>)
    }
}