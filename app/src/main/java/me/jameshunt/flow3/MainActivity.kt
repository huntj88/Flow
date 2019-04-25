package me.jameshunt.flow3

import me.jameshunt.flowandroid.FlowActivity
import me.jameshunt.flowandroid.FragmentFlowController
import me.jameshunt.flowandroid.FragmentGroupFlowController

class MainActivity : FlowActivity() {
    override fun getInitialFlow(): FragmentGroupFlowController = FragmentGroupFlowController(R.layout.group_top_bottom)

    override fun getInitialArgs(): FragmentGroupFlowController.FlowsInGroup = FragmentGroupFlowController.FlowsInGroup(
        mapOf(
            R.id.topLayout to RootFlowController::class.java as Class<FragmentFlowController<Unit, Unit>>,
            R.id.bottomLayout to RootFlowController::class.java as Class<FragmentFlowController<Unit, Unit>>
        )
    )
}
