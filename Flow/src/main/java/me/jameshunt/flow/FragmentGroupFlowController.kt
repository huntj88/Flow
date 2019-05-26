package me.jameshunt.flow

import android.view.ViewGroup
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.always
import me.jameshunt.flow.promise.then

abstract class FragmentGroupFlowController<Input : FragmentGroupFlowController.GroupInput, Output>(
    private val layoutId: LayoutId
) : FlowController<Input, Output>() {

    abstract class GroupInput

    protected object Back : BackState, State
    protected data class Done<Output>(override val output: Output) : FragmentFlowController.DoneState<Output>, State

    private var groupResult: Promise<Unit>? = null

    final override fun onStart(state: InitialState<Input>) {
        val layout = FlowManager.rootViewManager.setNewRoot(layoutId)
        setupGroup(layout)

        if (groupResult != null) return

        groupResult = startFlowInGroup(state.input).then {
            when (it) {
                is Back -> it.onBack()
                is Done<*> -> this@FragmentGroupFlowController.onDone(it.output as Output)
            }
        }
    }

    open fun setupGroup(layout: ViewGroup) {}

    abstract fun startFlowInGroup(groupInput: Input): Promise<State>

    fun <NewInput, NewOutput, Controller : FragmentFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        viewId: ViewId,
        input: NewInput
    ): Promise<FlowResult<NewOutput>> {
        val flowController = controller.newInstance().apply {
            (this as FragmentFlowController<NewInput, NewOutput>).viewId = viewId
        }

        childFlows.add(flowController)

        return flowController.launchFlow(input).always {
            childFlows.remove(flowController)
        }
    }

    protected open fun childIndexToDelegateBack(): Int = 0

    final override fun handleBack() {
        this.childFlows[childIndexToDelegateBack()].handleBack()
    }

    final override fun resume(currentState: State) {
        this.onStart(currentState as InitialState<Input>)
    }
}
