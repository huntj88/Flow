package me.jameshunt.flow3

import me.jameshunt.flow.FlowActivity
import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.FragmentGroupFlowController

class MainActivity : FlowActivity() {
    override fun getInitialFlow(): FragmentGroupFlowController = FragmentGroupFlowController(R.layout.group_top_bottom)

    override fun getInitialArgs(): FragmentGroupFlowController.FlowsInGroup = FragmentGroupFlowController.FlowsInGroup(
        mapOf(
            R.id.topLayout to RootFlowController::class.java as Class<FragmentFlowController<Unit, Unit>>,
            R.id.bottomLayout to RootFlowController::class.java as Class<FragmentFlowController<Unit, Unit>>
        )
    )
}
