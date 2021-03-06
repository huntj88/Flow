package me.jameshunt.flowtest

import com.inmotionsoftware.promisekt.PMKConfiguration
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.conf
import me.jameshunt.flow.*
import me.jameshunt.flow.BusinessFlowController

fun FragmentFlowController<*, *>.flowTest(configure: TestFlowFunctions.() -> Unit) {
    conf.Q.map?.let {
        conf.Q = PMKConfiguration.Value(null, null)
    }

    // set TestFlowFunctions for mocking fragments and other flowControllers
    FragmentFlowController::class.java.getDeclaredField("flowFunctions").let {
        it.isAccessible = true
        it.set(this, TestFlowFunctions().apply(configure))
    }
}

class TestFlowFunctions : AndroidFlowFunctions {

    private val mockedResults = mutableMapOf<Class<Any>, ((Any?) -> Any?)>()

    fun <FlowInput, FlowOutput, Controller> mockFlow(
        controller: Class<Controller>,
        thenReturn: (FlowInput) -> FlowOutput
    ) where Controller : FragmentFlowController<FlowInput, FlowOutput> {
        mockedResults[controller as Class<Any>] = thenReturn as (Any?) -> Any?
    }

    fun <FragInput, FragOutput, FragmentType> mockFragment(
        fragment: Class<FragmentType>,
        thenReturn: (FragInput) -> FragOutput
    ) where FragmentType : FlowUI<FragInput, FragOutput> {
        mockedResults[fragment as Class<Any>] = thenReturn as (Any?) -> Any?
    }

    private fun <In, Out>getAndroidFlowMock(clazz: Class<Any>, input: In): Promise<FlowResult<Out>> {
        val computeOutput = mockedResults[clazz] as? (In) -> Out

        return try {
            val output = computeOutput?.invoke(input)
                ?: throw IllegalArgumentException("Mock not setup for ${clazz.simpleName}")

            Promise.value(FlowResult.Completed(output))
        } catch (e: Exception) {
            Promise(e)
        }
    }

    override fun <NewInput, NewOutput, Controller : FragmentFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>> {
        return getAndroidFlowMock(controller as Class<Any>, input)
    }

    override fun <FragInput, FragOutput, FragmentType : FlowUI<FragInput, FragOutput>> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): Promise<FlowResult<FragOutput>> {

        val fragmentClass = fragmentProxy::class.java.getDeclaredField("clazz").let {
            it.isAccessible = true
            it.get(fragmentProxy) as Class<Any>
        }

        return getAndroidFlowMock(fragmentClass, input)
    }

    override fun <GroupInput, GroupOutput, Controller : FragmentGroupFlowController<GroupInput, GroupOutput>> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): Promise<FlowResult<GroupOutput>> {
        return getAndroidFlowMock(controller as Class<Any>, input)
    }

    override fun <NewInput, NewOutput, Controller : BusinessFlowController<NewInput, NewOutput>> flowBusiness(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>> {
        return getAndroidFlowMock(controller as Class<Any>, input)
    }
}

