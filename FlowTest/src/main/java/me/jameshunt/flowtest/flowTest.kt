package me.jameshunt.flowtest

import me.jameshunt.flow.BusinessFlowController
import me.jameshunt.flow.BusinessFlowFunctions

fun BusinessFlowController<*, *>.flowTest(configure: TestFlowFunctions.() -> Unit) {

    // set TestFlowFunctions for mocking fragments and other flowControllers
    BusinessFlowController::class.java.getDeclaredField("flowFunctions").let {
        it.isAccessible = true
        it.set(this, TestFlowFunctions().apply(configure))
    }
}

class TestFlowFunctions : BusinessFlowFunctions {

    private val mockedResults = mutableMapOf<Class<Any>, ((Any?) -> Any?)>()

    fun <FlowInput, FlowOutput, Controller> mockFlow(
        controller: Class<Controller>,
        thenReturn: (FlowInput) -> FlowOutput
    ) where Controller : BusinessFlowController<FlowInput, FlowOutput> {
        mockedResults[controller as Class<Any>] = thenReturn as (Any?) -> Any?
    }

    override suspend fun <NewInput, NewOutput, Controller : BusinessFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        input: NewInput
    ): NewOutput {
        val computeOutput = mockedResults[controller as Class<Any>] as? (NewInput) -> NewOutput

        return computeOutput?.invoke(input)
            ?: throw IllegalArgumentException("Mock not setup for ${controller.simpleName}")
    }
}

