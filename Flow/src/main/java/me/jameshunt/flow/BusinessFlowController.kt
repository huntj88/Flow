package me.jameshunt.flow

import com.inmotionsoftware.promisekt.Promise

interface BusinessFlowFunctions {
    fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<NewOutput>
            where Controller : BusinessFlowController<NewInput, NewOutput>
}

abstract class BusinessFlowController<Input, Output> : FlowController<Input, Output>(), BackgroundTask {

    private var flowFunctions: BusinessFlowFunctions = BusinessFlowFunctionsImpl()

    fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<NewOutput> where Controller : BusinessFlowController<NewInput, NewOutput> {
        return flowFunctions.flow(controller = controller, input = input)
    }

    inner class BusinessFlowFunctionsImpl : BusinessFlowFunctions {
        override fun <NewInput, NewOutput, Controller> flow(
            controller: Class<Controller>,
            input: NewInput
        ): Promise<NewOutput> where Controller : BusinessFlowController<NewInput, NewOutput> {

            val flowController = controller.newInstance()

            childFlows.add(flowController)

            return flowController.launchFlow(input).ensure {
                childFlows.remove(flowController)
            }
        }
    }
}