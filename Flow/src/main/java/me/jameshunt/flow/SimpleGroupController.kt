package me.jameshunt.flow

import me.jameshunt.flow.promise.Promise

class SimpleGroupController<Input, Output> :
    FragmentGroupFlowController<SimpleGroupController.SimpleGroupInput<Input, Output>, Output>(R.layout.group_simple) {

    class SimpleGroupInput<Input, Output>(
        val flow: Class<FragmentFlowController<Input, Output>>,
        val input: Input

    ) : FragmentGroupFlowController.GroupInput()

    override fun startFlowInGroup(groupInput: SimpleGroupInput<Input, Output>): Promise<State> {
        return this.flow(groupInput.flow, R.id.groupSimple, groupInput.input).forResult<Output, State>(
            onBack = { Promise(Back) },
            onComplete = { Promise(Done(it)) }
        )
    }
}