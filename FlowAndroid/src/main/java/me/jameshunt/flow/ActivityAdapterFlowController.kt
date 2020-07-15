package me.jameshunt.flow

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class ActivityAdapterFlowController<Input, Output> :
    FragmentFlowController<Input, Output>() {

    override fun onStart(state: InitialState<Input>) {
        GlobalScope.launch(Dispatchers.Main) {
            val activity = FlowManager.activityForResultManager.flowActivity
            handleIOActivityIntents(context = activity, flowInput = state.input).also {
                when (it) {
                    is FlowResult.Completed -> this@ActivityAdapterFlowController.onDone(
                        FlowResult.Completed(
                            it.data
                        )
                    )
                    is FlowResult.Back -> super.onDone(FlowResult.Back)
                }
            }
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
    abstract suspend fun handleIOActivityIntents(
        context: () -> Context,
        flowInput: Input
    ): FlowResult<Output>

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