package me.jameshunt.flow3.splash

import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.map
import me.jameshunt.flow.DeepLinkData
import me.jameshunt.flow.promise.DispatchExecutor
import me.jameshunt.flow.proxy
import me.jameshunt.flow3.ViewPagerGroupController
import me.jameshunt.flow3.root.RootFlowController
import me.jameshunt.flow3.splash.GeneratedSplashController.SplashFlowState.*
import me.jameshunt.flow3.summary.SummaryFlowController

class SplashFlowController: GeneratedSplashController() {

    private val splashFragmentProxy = proxy(SplashFragment::class.java)

    private lateinit var deepLinkData: DeepLinkData

    override fun onSplash(state: Splash): Promise<FromSplash> {
        deepLinkData = state.deepLinkData

        return this.flow(fragmentProxy = splashFragmentProxy, input = Unit).forResult<Unit, FromSplash>(
            onComplete = { Promise.value(Load) }
        )
    }

    override fun onLoad(state: Load): Promise<FromLoad> {
        return Promise.value(Unit)
            .map(on = DispatchExecutor.global) { Thread.sleep(1000) }
            .map { FinishedLoading }
    }

    override fun onFinishedLoading(state: FinishedLoading): Promise<FromFinishedLoading> {
        val input = ViewPagerGroupController.input(
            pageZero = SummaryFlowController::class.java,
            pageOne = RootFlowController::class.java,
            pageTwo = RootFlowController::class.java
        )

        return when(deepLinkData.intentBundle == null) {
            true -> this.flowGroup(ViewPagerGroupController::class.java, input).forResult<Unit, FromFinishedLoading>(
                onBack = { Promise.value(Done(Unit)) },
                onComplete = { Promise.value(Done(Unit)) }
            )
            false -> this.flow(fragmentProxy = splashFragmentProxy, input = Unit).forResult<Unit, FromFinishedLoading>(
                onBack = { TODO() },
                onComplete = { TODO() }
            )
        }
    }
}
