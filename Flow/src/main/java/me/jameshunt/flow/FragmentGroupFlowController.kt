package me.jameshunt.flow

import android.view.ViewGroup
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.always

abstract class FragmentGroupFlowController<T>(internal val layoutId: LayoutId): FlowController<FragmentGroupFlowController.FlowsInGroup<T>, Unit>() {

    data class FlowsInGroup<T>(
        val map: Map<ViewId, Class<FragmentFlowController<Unit, Unit>>>,
        val extra: T
    )

    object Back: BackState

    override fun onStart(state: InitialState<FlowsInGroup<T>>) {
        val layout = FlowManager.rootViewManager.get()!!.setNewRoot(layoutId)
        setupGroup(layout, state.arg)

        if(childFlows.isEmpty()) {
            state.arg.map.forEach { (viewId, flowController) ->
                this.flow(controller = flowController, viewId = viewId, arg = Unit).always {
                    Back.onBack()
                }
            }
        }
    }

    open fun setupGroup(layout: ViewGroup, flowsInGroup: FlowsInGroup<T>) {}

    fun <NewInput, NewOutput, Controller: FragmentFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        viewId: ViewId,
        arg: NewInput
    ): Promise<FlowResult<NewOutput>> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(arg).always {
            childFlows.remove(flowController)
        }
    }

    protected open fun childIndexToDelegateBack(): Int = 0

    override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows[childIndexToDelegateBack()].handleBack()
    }

    override fun resume(currentState: State) {
        this.onStart(currentState as InitialState<FlowsInGroup<T>>)
    }
}

fun <T: FragmentFlowController<Unit, Unit>> Class<T>.putInView(viewId: ViewId): Pair<ViewId, Class<FragmentFlowController<Unit,Unit>>> = Pair(viewId, this as Class<FragmentFlowController<Unit, Unit>>)