package me.jameshunt.flow

interface BusinessFlowFunctions {
    suspend fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): NewOutput
            where Controller : BusinessFlowController<NewInput, NewOutput>
}

abstract class BusinessFlowController<Input, Output> : FlowController<Input, Output>() {

    private var flowFunctions: BusinessFlowFunctions = BusinessFlowFunctionsImpl()

    protected suspend fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): NewOutput where Controller : BusinessFlowController<NewInput, NewOutput> {
        return flowFunctions.flow(controller = controller, input = input)
    }

    private inner class BusinessFlowFunctionsImpl : BusinessFlowFunctions {
        override suspend fun <NewInput, NewOutput, Controller> flow(
            controller: Class<Controller>,
            input: NewInput
        ): NewOutput where Controller : BusinessFlowController<NewInput, NewOutput> {

            val flowController = controller.newInstance()

            childFlows.add(flowController)
            val output = flowController.launchFlow(input)
            childFlows.remove(flowController)

            return output
        }
    }
}