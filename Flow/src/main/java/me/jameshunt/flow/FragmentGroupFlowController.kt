package me.jameshunt.flow

import android.view.ViewGroup
import me.jameshunt.flow.promise.always

abstract class FragmentGroupFlowController<T>(internal val layoutId: LayoutId): FlowController<FragmentGroupFlowController.FlowsInGroup<T>, Unit>() {

    data class FlowsInGroup<T>(
        val map: Map<ViewId, Class<FragmentFlowController<Unit, Unit>>>,
        val extra: T
    )

    override fun onStart(state: InitialState<FlowsInGroup<T>>) {
        val layout = FlowManager.rootViewManager.get()!!.setNewRoot(layoutId)
        setupGroup(layout, state.arg)

        if(childFlows.isEmpty()) {
            state.arg.map.forEach { (viewId, flowController) ->
                this.flow(controller = flowController, viewId = viewId, arg = Unit).always {
                    this.onBack()
                }
            }
        }
    }

    open fun setupGroup(layout: ViewGroup, flowsInGroup: FlowsInGroup<T>) {}

    protected open fun childIndexToDelegateBack(): Int = 0

    override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows[childIndexToDelegateBack()].handleBack()
    }

    override fun resume(currentState: State) {
        this.onStart(currentState as InitialState<FlowsInGroup<T>>)
    }
}

inline fun <reified T: FragmentFlowController<Unit, Unit>> ViewId.toPair(controllerClass: Class<T>): Pair<ViewId, Class<FragmentFlowController<Unit,Unit>>> = Pair(this, controllerClass as Class<FragmentFlowController<Unit, Unit>>)