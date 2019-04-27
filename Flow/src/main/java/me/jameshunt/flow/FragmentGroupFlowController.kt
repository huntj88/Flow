package me.jameshunt.flow

import android.view.ViewGroup
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.always

abstract class FragmentGroupFlowController<T>(internal val layoutId: LayoutId): FlowController<FragmentGroupFlowController.FlowsInGroup<T>, Unit>() {

    data class FlowsInGroup<T>(
        val map: Map<ViewId, Class<FragmentFlowController<*, *>>>,
        val extra: T
    )

    object Back: BackState, State
    data class Done(override val output: Unit): FragmentFlowController.DoneState<Unit>, State

    final override fun onStart(state: InitialState<FlowsInGroup<T>>) {
        val layout = FlowManager.rootViewManager.setNewRoot(layoutId)
        setupGroup(layout, state.input)

        if(childFlows.isEmpty()) {
            state.input.map.forEach { (viewId, flowController) ->

                when(this is DeepLinkGroupController){
                    true -> this.flow(
                        controller = flowController as Class<FragmentFlowController<DeepLinkData, Unit>>,
                        viewId = viewId,
                        input = this.deepLinkData
                    )
                    false -> this.flow(
                        controller = flowController as Class<FragmentFlowController<Unit, Unit>>,
                        viewId = viewId,
                        input = Unit
                    )
                }.forResult<Unit, State>(
                    onBack = { Promise(Back) },
                    onComplete = { Promise(Done(Unit)) }
                )
            }
        }
    }

    open fun setupGroup(layout: ViewGroup, flowsInGroup: FlowsInGroup<T>) {}

    fun <NewInput, NewOutput, Controller: FragmentFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        viewId: ViewId,
        input: NewInput
    ): Promise<FlowResult<NewOutput>> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(input).always {
            childFlows.remove(flowController)
        }
    }

    protected open fun childIndexToDelegateBack(): Int = 0

    final override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows[childIndexToDelegateBack()].handleBack()
    }

    final override fun resume(currentState: State) {
        this.onStart(currentState as InitialState<FlowsInGroup<T>>)
    }
}

fun <T: FragmentFlowController<Unit, Unit>> Class<T>.putInView(viewId: ViewId): Pair<ViewId, Class<FragmentFlowController<*,*>>> = Pair(viewId, this as Class<FragmentFlowController<*, *>>)