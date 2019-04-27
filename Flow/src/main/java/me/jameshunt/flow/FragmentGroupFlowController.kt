package me.jameshunt.flow

import android.view.ViewGroup
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.always
import me.jameshunt.flow.promise.then

abstract class FragmentGroupFlowController<T>(internal val layoutId: LayoutId) :
    FlowController<FragmentGroupFlowController.GroupFlows, Unit>() {

    interface GroupFlows

    internal data class DeepLinkFlowGroup(
        val viewId: ViewId,
        val deepLinkFlow: Class<FragmentFlowController<DeepLinkData, Unit>>,
        val deepLinkData: DeepLinkData
    ) : GroupFlows

    data class FlowsInGroup<T>(
        val map: Map<ViewId, Class<FragmentFlowController<Unit, Unit>>>,
        val extra: T
    ) : GroupFlows

    object Back : BackState, State
    data class Done(override val output: Unit) : FragmentFlowController.DoneState<Unit>, State

    final override fun onStart(state: InitialState<GroupFlows>) {
        val layout = FlowManager.rootViewManager.setNewRoot(layoutId)

        when (val input = state.input) {
            is DeepLinkFlowGroup -> deepLinkGroup(state.input as DeepLinkFlowGroup)
            is FlowsInGroup<*> -> normalGroup(layout, input as FlowsInGroup<T>)
            else -> throw IllegalStateException("Group Flow input not supported")
        }
    }

    private fun deepLinkGroup(input: DeepLinkFlowGroup) {
        this.flow(input.deepLinkFlow, input.viewId, input.deepLinkData).handleFlowResult()
    }

    private fun normalGroup(layout: ViewGroup, flowsInGroup: FlowsInGroup<T>) {
        setupGroup(layout, flowsInGroup)

        if (childFlows.isEmpty()) {
            flowsInGroup.map.forEach { (viewId, flowController) ->
                this.flow(controller = flowController, viewId = viewId, input = Unit).handleFlowResult()
            }
        }
    }

    private fun Promise<FlowResult<Unit>>.handleFlowResult() = this
        .forResult<Unit, State>(
            onBack = { Promise(Back) },
            onComplete = { Promise(Done(Unit)) }
        )
        .then {
            when (it) {
                is Back -> it.onBack()
                is Done -> this@FragmentGroupFlowController.onDone(it.output)
            }
        }

    open fun setupGroup(layout: ViewGroup, flowsInGroup: FlowsInGroup<T>) {}

    fun <NewInput, NewOutput, Controller : FragmentFlowController<NewInput, NewOutput>> flow(
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
        this.onStart(currentState as InitialState<GroupFlows>)
    }
}

fun <T : FragmentFlowController<Unit, Unit>> Class<T>.putInView(viewId: ViewId): Pair<ViewId, Class<FragmentFlowController<Unit, Unit>>> =
    Pair(viewId, this as Class<FragmentFlowController<Unit, Unit>>)