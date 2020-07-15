package me.jameshunt.flow

import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class FragmentGroupFlowController<Input, Output>(
    private val layoutId: LayoutId
) : AndroidFlowController<Input, Output>() {

    protected object Back : State
    protected data class Done<Output>(override val output: Output) : DoneState<Output>, State

    private var groupResult: Job? = null

    private var initialState: InitialState<Input>? = null

    final override fun onStart(state: InitialState<Input>) {
        initialState = state
        val layout = FlowManager.rootViewManager.setNewRoot(layoutId)
        setupGroup(layout)

        if (groupResult != null) return


        //TODO no global scope here
        groupResult = GlobalScope.launch(Dispatchers.Main) {
            startFlowInGroup(state.input).also {
                when (it) {
                    is Back -> super.onDone(FlowResult.Back)
                    is Done<*> -> {
                        val output = FlowResult.Completed(it.output) as FlowResult<Output>
                        super.onDone(output)
                    }
                }
            }
        }
    }

    open fun setupGroup(layout: ViewGroup) {}

    abstract suspend fun startFlowInGroup(groupInput: Input): State

    suspend fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        viewId: ViewId,
        input: NewInput
    ): FlowResult<NewOutput>
            where Controller : FragmentFlowController<NewInput, NewOutput> {

        val flowController = controller.newInstance().apply {
            this.viewId = viewId
        }

        childFlows.add(flowController)

        return try {
            flowController.launchFlow(input).await()
        } finally {
            childFlows.remove(flowController)
        }
    }

    protected open fun childIndexToDelegateBack(): Int = 0

    final override fun handleBack() {
        this.childFlows[childIndexToDelegateBack()]
            .let { it as AndroidFlowController<*, *> }
            .handleBack()
    }

    final override fun resume() {
        this.onStart(initialState as InitialState<Input>)
    }
}
