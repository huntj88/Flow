package me.jameshunt.flow

import com.inmotionsoftware.promisekt.Promise

class SimpleGroupController<Input, Output> :
    FragmentGroupFlowController<SimpleGroupController.SimpleGroupInput<Input, Output>, Output>(R.layout.group_simple) {

    companion object {
        fun <Flow, Input, Output> input(
            flow: Class<Flow>,
            input: Input
        ): SimpleGroupInput<Input, Output>
                where Flow : FragmentFlowController<Input, Output> {
            return SimpleGroupInput(
                flow = flow as Class<FragmentFlowController<Input, Output>>,
                input = input
            )
        }
    }

    class SimpleGroupInput<Input, Output>(
        val flow: Class<FragmentFlowController<Input, Output>>,
        val input: Input
    )

    override fun startFlowInGroup(groupInput: SimpleGroupInput<Input, Output>): Promise<State> {
        return this.flow(groupInput.flow, R.id.groupSimple, groupInput.input).forResult<Output, State>(
            onBack = { Promise.value(Back) },
            onComplete = { Promise.value(Done(it)) }
        )
    }
}

fun <Input, Output> Class<SimpleGroupController<*, *>>.castFromInput(
    simpleGroupInput: SimpleGroupController.SimpleGroupInput<Input, Output>
): Class<FragmentGroupFlowController<SimpleGroupController.SimpleGroupInput<Input, Output>, Output>> {
    return this as Class<FragmentGroupFlowController<SimpleGroupController.SimpleGroupInput<Input, Output>, Output>>
}