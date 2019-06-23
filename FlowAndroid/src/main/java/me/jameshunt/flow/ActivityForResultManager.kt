package me.jameshunt.flow

import android.content.Intent
import com.inmotionsoftware.promisekt.DeferredPromise
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.ensure

const val ACTIVITY_FOR_RESULT = 136

internal class ActivityForResultManager(val flowActivity: () -> FlowActivity<*>) {

    private var activityResultPromise: DeferredPromise<FlowResult<Any?>> = DeferredPromise()
    private var resultHandler: ((Intent) -> Any?)? = null

    fun <ActivityOutput> activityForResult(
        intent: Intent,
        handleResult: (result: Intent) -> ActivityOutput
    ): Promise<FlowResult<ActivityOutput>> {

        resultHandler = handleResult

        flowActivity().startActivityForResult(intent, ACTIVITY_FOR_RESULT)

        return (activityResultPromise.promise as Promise<FlowResult<ActivityOutput>>).ensure {
            resultHandler = null
            activityResultPromise = DeferredPromise()
        }
    }

    fun onActivityResult(data: Intent?) {
        val promiseOutput = when(data) {
            null -> FlowResult.Back
            else -> FlowResult.Completed(resultHandler!!(data))
        }

        activityResultPromise.resolve(promiseOutput)
    }
}
