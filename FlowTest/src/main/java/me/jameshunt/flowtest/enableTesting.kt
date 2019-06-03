package me.jameshunt.flowtest

import com.inmotionsoftware.promisekt.PMKConfiguration
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.conf
import me.jameshunt.flow.FlowResult
import me.jameshunt.flow.FlowUI
import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.FragmentProxy

fun FragmentFlowController<*, *>.enableTesting(configure: TestFlowFunctions.() -> Unit) {
    conf.Q = PMKConfiguration.Value(null, null)

    // set TestFlowFunctions for mocking fragments and other flowControllers
    FragmentFlowController::class.java.getDeclaredField("flowFunctions").let {
        it.isAccessible = true
        it.set(this, TestFlowFunctions().apply(configure))
    }
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

        val fragmentClass = fragmentProxy::class.java.getDeclaredField("clazz").let {
            it.isAccessible = true
            it.get(fragmentProxy) as Class<Any>
        }

        val computeOutput = fragmentResults[fragmentClass] as? (FragInput) -> FragOutput

        val output = computeOutput?.invoke(input)
            ?: throw IllegalArgumentException("Mock not setup for ${fragmentClass.simpleName}")

        return Promise.value(FlowResult.Completed(output))
    }

}

