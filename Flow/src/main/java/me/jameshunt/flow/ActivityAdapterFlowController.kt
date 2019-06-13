package me.jameshunt.flow

import android.content.Context
import android.content.Intent
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.done

abstract class ActivityAdapterFlowController<Input, Output> : FragmentFlowController<Input, Output>() {

    override fun onStart(state: InitialState<Input>) {
        handleInputOutputIntents(state.input).done {
            when (it) {
                is FlowResult.Completed -> this.onDone(FlowResult.Completed(it.data))
                is FlowResult.Back -> (object : BackState {}).onBack()
            }
        }
    }

    abstract fun handleInputOutputIntents(flowInput: Input): Promise<FlowResult<Output>>

    fun <Output> flow(
        activityIntent: Intent,
        handleResult: (Context, result: Intent) -> Output
    ): Promise<FlowResult<Output>> {
        return FlowManager.activityForResultManager.activityForResult(
            intent = activityIntent,
            handleResult = handleResult
        )
    }
}