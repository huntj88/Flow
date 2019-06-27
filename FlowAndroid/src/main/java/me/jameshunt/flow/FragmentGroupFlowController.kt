package me.jameshunt.flow

import android.view.ViewGroup

abstract class FragmentGroupFlowController<Input, Output>(
    private val layoutId: LayoutId
) : AndroidFlowController<Input, Output>() {

    protected object Back : BackState, State
    protected data class Done<Output>(override val output: Output) : DoneState<Output>, State

    private var isResumed = false

    private var initialState: InitialState<Input>? = null

    final override suspend fun onStart(state: InitialState<Input>) {
        initialState = state
        val layout = FlowManager.rootViewManager.setNewRoot(layoutId)
        setupGroup(layout)

        if (isResumed) return

        // todo error handling
        when (val result = startFlowInGroup(state.input)) {
            is Back -> this.onDone(FlowResult.Back)
            is Done<*> -> {
                val output = FlowResult.Completed(result.output) as FlowResult<Output>
                this.onDone(output)
            }
        }
    }

    open fun setupGroup(layout: ViewGroup) {}

    abstract suspend fun startFlowInGroup(groupInput: Input): State

    suspend fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        viewId: ViewId,
        input: NewInput
    ): suspend () -> FlowResult<NewOutput>
            where Controller : FragmentFlowController<NewInput, NewOutput> {
        return {
            val flowController = controller.newInstance().apply {
                this.viewId = viewId
            }

            childFlows.add(flowController)

            try {
                flowController.launchFlow(input)
            } catch (t: Throwable) {
                onCatch(t); Back as FlowResult<NewOutput> // back ignored
            } finally {
                childFlows.remove(flowController)
            }
        }
    }

    protected open fun childIndexToDelegateBack(): Int = 0

    final override fun handleBack() {
        this.childFlows[childIndexToDelegateBack()]
            .let { it as AndroidFlowController<*, *> }
            .handleBack()
    }

    final override suspend fun resume() {
        isResumed = true
        onStart(initialState as InitialState<Input>)
    }
}
