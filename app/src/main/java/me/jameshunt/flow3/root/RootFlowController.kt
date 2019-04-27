package me.jameshunt.flow3.root

import android.util.Log
import me.jameshunt.flow.*
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.PromiseDispatch
import me.jameshunt.flow.promise.doAlso
import me.jameshunt.flow3.TestFragment
import me.jameshunt.flow3.root.GeneratedRootFlow.RootFlowState.*
import me.jameshunt.flow3.splash.SplashFragment

class RootFlowController(viewId: ViewId) : GeneratedRootFlow(viewId) {

    private val testFragmentProxy = proxy(TestFragment::class.java)
    private val splashFragmentProxy = proxy(SplashFragment::class.java)

    override fun onOne(state: One): Promise<FromOne> {
        return this.flow(fragmentProxy = this.testFragmentProxy, input = "wow").forResult<Unit, FromOne>(
            onBack = { Promise(Three("wow")) },
            onComplete = {
                // wrapper in a Promise in case you need to do some async stuff
                Promise(Four("complete"))
            }
        )
    }

    override fun onTwo(state: Two): Promise<FromTwo> {
        return Promise(Back)
    }

    override fun onThree(state: Three): Promise<FromThree> {
        Log.d("three", "going to 2")
        return Promise(Two("no"))
    }

    override fun onFour(state: Four): Promise<FromFour> {
        return this.flow(fragmentProxy = this.splashFragmentProxy, input = Unit)
            .doAlso(on = PromiseDispatch.BACKGROUND) { Thread.sleep(1000) }
            .forResult<Unit, FromFour>(
                onBack = { TODO() },
                onComplete = {
                    // wrapper in a Promise in case you need to do some async stuff
                    Promise(One)
                }
            )
    }
}

