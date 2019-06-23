package me.jameshunt.flow

import com.inmotionsoftware.promisekt.Promise
import java.util.concurrent.atomic.AtomicBoolean

interface BusinessFlowFunctions {
    fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<NewOutput>
            where Controller : BusinessFlowController<NewInput, NewOutput>
}

abstract class BusinessFlowController<Input, Output> : FlowController<Input, Output>(), BackgroundTask {

    private var flowFunctions: BusinessFlowFunctions = BusinessFlowFunctionsImpl()

    protected fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<NewOutput> where Controller : BusinessFlowController<NewInput, NewOutput> {
        return flowFunctions.flow(controller = controller, input = input)
    }

    private val cancelled = AtomicBoolean(false)
    protected fun <T> Promise<T>.handleFlowCancel(): Promise<T> {
        when (cancelled.get()) {
            true -> this@handleFlowCancel.cancel()
            false -> resultPromise.promise.apply {
                this@apply.onCancel {
                    cancelled.set(true)
                    this@handleFlowCancel.cancel()
                }
            }
        }

        return this
    }

    private inner class BusinessFlowFunctionsImpl : BusinessFlowFunctions {
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