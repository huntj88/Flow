package me.jameshunt.flow3.splash

import me.jameshunt.flow.*
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.PromiseDispatch
import me.jameshunt.flow.promise.then
import me.jameshunt.flow3.R
import me.jameshunt.flow3.ViewPagerGroupController
import me.jameshunt.flow3.root.RootFlowController
import me.jameshunt.flow3.splash.GeneratedSplashController.SplashFlowState.*
import me.jameshunt.flow3.summary.SummaryFlowController

class SplashFlowController(viewId: ViewId): GeneratedSplashController(viewId) {

    private val splashFragmentProxy = proxy(SplashFragment::class.java)

    private lateinit var deepLinkData: DeepLinkData

    override fun onSplash(state: Splash): Promise<FromSplash> {
        deepLinkData = state.deepLinkData

        return this.flow(fragmentProxy = splashFragmentProxy, input = Unit).forResult<Unit, FromSplash>(
            onBack = { TODO() },
            onComplete = { Promise(Load) }
        )
    }

    override fun onLoad(state: Load): Promise<FromLoad> {
        return Promise(Unit)
            .then(on = PromiseDispatch.BACKGROUND) { Thread.sleep(1000) }
            .then { FinishedLoading }
    }

    override fun onFinishedLoading(state: FinishedLoading): Promise<FromFinishedLoading> {
        val flowsInGroup = mapOf(
            SummaryFlowController::class.java.putInView(R.id.groupPagerZero),
            RootFlowController::class.java.putInView(R.id.groupPagerOne),
            RootFlowController::class.java.putInView(R.id.groupPagerTwo)
        )
        val groupArgs = FragmentGroupFlowController.FlowsInGroup(flowsInGroup, Unit)

        return when(deepLinkData.intentBundle == null) {
            true -> this.flowGroup(ViewPagerGroupController::class.java, groupArgs).forResult<Unit, FromFinishedLoading>(
                onBack = { Promise(Done(Unit)) },
                onComplete = { Promise(Done(Unit)) }
            )
            false -> this.flow(fragmentProxy = splashFragmentProxy, input = Unit).forResult<Unit, FromFinishedLoading>(
                onBack = { TODO() },
                onComplete = { TODO() }
            )
        }
    }
}
