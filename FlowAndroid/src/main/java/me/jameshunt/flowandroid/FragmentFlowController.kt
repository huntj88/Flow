package me.jameshunt.flowandroid

import me.jameshunt.flow.FlowController
import me.jameshunt.flow.Promise

typealias ViewId = Int

abstract class FragmentFlowController<Input, Output>(val viewId: ViewId) : FlowController<Input, Output>() {

    protected fun <NewInput, NewOutput> flow(
        controller: Class<FragmentFlowController<NewInput, NewOutput>>,
        viewId: ViewId,
        arg: NewInput
    ): Promise<NewOutput> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(arg)
    }
}