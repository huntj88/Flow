package me.jameshunt.flow

import android.view.ViewGroup
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.ensure
import com.inmotionsoftware.promisekt.map

abstract class FragmentGroupFlowController<Input, Output>(
    private val layoutId: LayoutId
) : FragmentFlowController<Input, Output>() {

    protected object Back : BackState, State
    protected data class Done<Output>(override val output: Output) : DoneState<Output>, State

    private var groupResult: Promise<Unit>? = null

    final override fun onStart(state: InitialState<Input>) {
        val layout = FlowManager.rootViewManager.setNewRoot(layoutId)
        setupGroup(layout)

        if (groupResult != null) return

        groupResult = startFlowInGroup(state.input).map {
            when (it) {
                is Back -> it.onBack()
                is Done<*> -> this@FragmentGroupFlowController.onDone(it.output as FlowResult<Output>)
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

        return flowController.launchFlow(input).ensure {
            childFlows.remove(flowController)
        }
    }

    protected open fun childIndexToDelegateBack(): Int = 0

    final override fun handleBack() {
        this.childFlows[childIndexToDelegateBack()]
            .let { it as FragmentFlowController<*, *> }
            .handleBack()
    }

    final override fun resume(currentState: State) {
        this.onStart(currentState as InitialState<Input>)
    }
}

fun <GroupInput, GroupOutput, Controller> FragmentFlowController<*, *>.flowGroup(
    controller: Class<Controller>,
    input: GroupInput
): Promise<FlowResult<GroupOutput>>
        where Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {

    // remove all the fragments from this flowController before starting the next FlowController
    // (state will still be saved when they get back)
    // The fragments parent views could potentially no longer exist
    FlowManager.fragmentDisplayManager.removeAll()

    val flowController = controller.newInstance()

    childFlows.add(flowController)

    return flowController.launchFlow(input).ensure {
        childFlows.remove(flowController)
        FlowManager.resumeActiveFlowControllers()
    }
}
