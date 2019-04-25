package me.jameshunt.flow3

import me.jameshunt.flowandroid.FlowActivity
import me.jameshunt.flowandroid.FragmentFlowController

class MainActivity : FlowActivity() {
    override fun getInitialFlow(): FragmentFlowController<Unit, Unit> = RootFlowController(R.id.initialFrameLayout)
}
