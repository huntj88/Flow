package me.jameshunt.flow

import android.content.Context
import android.content.Intent
import com.inmotionsoftware.promisekt.DeferredPromise
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.ensure

const val ACTIVITY_FOR_RESULT = 136

class ActivityForResultManager(val flowActivity: () -> FlowActivity<*>) {

    // for activities that return a result, like an image picker
    private var activityResultPromise: DeferredPromise<FlowResult<Any?>> = DeferredPromise()
    private var resultHandler: ((Context, Intent) -> Any?)? = null

    fun <ActivityOutput> activityForResult(
        intent: Intent,
        handleResult: (Context, data: Intent) -> ActivityOutput
    ): Promise<FlowResult<ActivityOutput>> {

        resultHandler = handleResult

        flowActivity().startActivityForResult(intent, ACTIVITY_FOR_RESULT)

        return (activityResultPromise.promise as Promise<FlowResult<ActivityOutput>>).ensure {
            resultHandler = null
            activityResultPromise = DeferredPromise()
        }
    }

    fun onActivityResult(data: Intent?) {

        val promiseOutput = data?.let {
            val output = resultHandler!!(flowActivity(), data)
            FlowResult.Completed(output)
        } ?: FlowResult.Back

        activityResultPromise.resolve(promiseOutput)
    }
}
