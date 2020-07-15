package me.jameshunt.flow

import android.content.Intent
import kotlinx.coroutines.CompletableDeferred

const val ACTIVITY_FOR_RESULT = 136

internal class ActivityForResultManager(val flowActivity: () -> FlowActivity<*>) {

    private var activityResultPromise: CompletableDeferred<FlowResult<Any?>> = CompletableDeferred()
    private var resultHandler: ((Intent) -> Any?)? = null

    suspend fun <ActivityOutput> activityForResult(
        intent: Intent,
        handleResult: (result: Intent) -> ActivityOutput
    ): FlowResult<ActivityOutput> {

        resultHandler = handleResult

        flowActivity().startActivityForResult(intent, ACTIVITY_FOR_RESULT)

        return try {
            (activityResultPromise.await() as FlowResult<ActivityOutput>)
        } finally {
            resultHandler = null
            activityResultPromise = CompletableDeferred()
        }
    }

    fun onActivityResult(data: Intent?) {
        val promiseOutput = when (data) {
            null -> FlowResult.Back
            else -> FlowResult.Completed(resultHandler!!(data))
        }

        activityResultPromise.complete(promiseOutput)
    }
}
