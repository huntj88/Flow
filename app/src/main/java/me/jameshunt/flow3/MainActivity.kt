package me.jameshunt.flow3

import me.jameshunt.flow.FlowActivity
import me.jameshunt.flow3.splash.SplashFlowController

class MainActivity : FlowActivity<SplashFlowController>() {
    override fun getInitialFlow(): Class<SplashFlowController> = SplashFlowController::class.java
}
