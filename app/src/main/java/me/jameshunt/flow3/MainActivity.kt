package me.jameshunt.flow3

import me.jameshunt.flow.FlowActivity

class MainActivity : FlowActivity<RootFlowController>() {
    override fun getInitialFlow(): Class<RootFlowController> = RootFlowController::class.java
}
