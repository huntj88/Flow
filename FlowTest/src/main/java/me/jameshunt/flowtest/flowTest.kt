package me.jameshunt.flowtest

import com.inmotionsoftware.promisekt.PMKConfiguration
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.conf
import me.jameshunt.flow.*
import me.jameshunt.flow.BusinessFlowController
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties

fun BusinessFlowController<*, *>.flowTest(configure: TestFlowFunctions.() -> Unit) {
    conf.Q.map?.let {
        conf.Q = PMKConfiguration.Value(null, null)
    }

    BusinessFlowController::class
        .companionObject!!
        .memberProperties
        .first { it.name == "backgroundExecutor" }
        .let { companionExecutor ->
            if (companionExecutor is KMutableProperty<*>) {
                val instance = BusinessFlowController::class.companionObjectInstance
                companionExecutor.setter.call(instance, null)
            }
        }

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

    override fun <NewInput, NewOutput, Controller : BusinessFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<NewOutput> {
        val computeOutput = mockedResults[controller as Class<Any>] as? (NewInput) -> NewOutput

        return try {
            val output = computeOutput?.invoke(input)
                ?: throw IllegalArgumentException("Mock not setup for ${controller.simpleName}")

            Promise.value(output)
        } catch (e: Exception) {
            Promise(e)
        }
    }
}

