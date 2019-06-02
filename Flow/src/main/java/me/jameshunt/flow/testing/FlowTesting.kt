package me.jameshunt.flow.testing

import com.inmotionsoftware.promisekt.Promise
import me.jameshunt.flow.FlowResult
import me.jameshunt.flow.FlowUI
import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.FragmentProxy
import me.jameshunt.flow.promise.DispatchExecutor

fun FragmentFlowController<*, *>.enableTesting(configure: TestFlowFunctions.() -> Unit) {
    DispatchExecutor.setTestExecutor()
    this.flowFunctions = TestFlowFunctions().apply(configure)
}

class TestFlowFunctions : FragmentFlowController.FlowFunctions {

    private val fragmentResults = mutableMapOf<Class<Any>, ((Any?) -> Any?)>()

    fun <FragInput, FragOutput, FragmentType> mockFragment(
        fragment: Class<FragmentType>,
        thenReturn: (FragInput) -> FragOutput
    ) where FragmentType : FlowUI<FragInput, FragOutput> {
        fragmentResults[fragment as Class<Any>] = thenReturn as (Any?) -> Any?
    }

    override fun <FragInput, FragOutput, FragmentType : FlowUI<FragInput, FragOutput>> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): Promise<FlowResult<FragOutput>> {
        val fragmentClass = fragmentProxy.clazz as Class<Any>
        val computeOutput = fragmentResults[fragmentClass] as? (FragInput) -> FragOutput

        val output = computeOutput?.invoke(input)
            ?: throw IllegalArgumentException("Mock not setup for ${fragmentProxy.clazz.simpleName}")

        return Promise.value(FlowResult.Completed(output))
    }

}

