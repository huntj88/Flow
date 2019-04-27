package me.jameshunt.flow3

import me.jameshunt.flow.FlowActivity
import me.jameshunt.flow3.root.RootFlowController

class MainActivity : FlowActivity<RootFlowController>() {
    override fun getInitialFlow(): Class<RootFlowController> = RootFlowController::class.java
}
