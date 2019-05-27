package me.jameshunt.flow3.root

import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.ensure
import com.inmotionsoftware.promisekt.map
import me.jameshunt.flow.promise.DispatchExecutor
import me.jameshunt.flow.proxy
import me.jameshunt.flow3.FormFragment
import me.jameshunt.flow3.TestFragment
import me.jameshunt.flow3.root.GeneratedRootFlow.RootFlowState.*
import me.jameshunt.flow3.splash.SplashFragment

class RootFlowController : GeneratedRootFlow() {

    private val testFragmentProxy = proxy(TestFragment::class.java)
    private val splashFragmentProxy = proxy(SplashFragment::class.java)
    private val formFragmentProxy = proxy(FormFragment::class.java)

    override fun onOne(state: One): Promise<FromOne> {
        return this.flow(fragmentProxy = this.testFragmentProxy, input = "wow").forResult<Unit, FromOne>(
            onBack = { Promise.value(Three("wow")) },
            onComplete = {
                // wrapper in a Promise in case you need to do some async stuff
                Promise.value(Four("complete"))
            }
        )
    }

    override fun onTwo(state: Two): Promise<FromTwo> {
        return Promise.value(Back)
    }

    override fun onThree(state: Three): Promise<FromThree> {
        return this.flow(fragmentProxy = this.formFragmentProxy, input = Unit).forResult<Unit, FromThree>(
            onComplete = {
                // wrapper in a Promise in case you need to do some async stuff
                Promise.value(Two("complete"))
            }
        )
    }

    override fun onFour(state: Four): Promise<FromFour> {
        return this.flow(fragmentProxy = this.splashFragmentProxy, input = Unit)
            .ensure(on = DispatchExecutor.global) { Thread.sleep(1000) }
            .forResult<Unit, FromFour>(
                onComplete = {
                    // wrapper in a Promise in case you need to do some async stuff
                    Promise.value(One)
                }
            )
    }
}

