package me.jameshunt.flow

import android.content.Context
import android.content.Intent

abstract class ActivityAdapterFlowController<Input, Output> : FragmentFlowController<Input, Output>() {

    override suspend fun onStart(state: InitialState<Input>) {
        val activity = FlowManager.activityForResultManager.flowActivity
        val activityResult = handleIOActivityIntents(context = activity, flowInput = state.input)

        when (activityResult) {
            is FlowResult.Completed -> this.onDone(FlowResult.Completed(activityResult.data))
            is FlowResult.Back -> this.onDone(FlowResult.Back)
        }
    }

    /**
     * Purpose is to construct the input intent, and pass it into
     *
     * `this.flow(Intent) { /* then handle the output intent */ }`
     *
     *
     * Whenever a context is needed call the provided lambda.
     *
     * Instead of keeping a reference to a context,
     * invoke the context lambda as many times a needed
     */
    abstract suspend fun handleIOActivityIntents(context: () -> Context, flowInput: Input): FlowResult<Output>

    protected suspend fun <Output> flow(
        activityIntent: Intent,
        handleResult: (result: Intent) -> Output
    ): FlowResult<Output> {
        return FlowManager.activityForResultManager.activityForResult(
            intent = activityIntent,
            handleResult = handleResult
        )
    }
}