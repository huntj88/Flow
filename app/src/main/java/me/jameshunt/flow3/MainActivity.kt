package me.jameshunt.flow3

import me.jameshunt.flowandroid.FlowActivity
import me.jameshunt.flowandroid.FragmentFlowController
import me.jameshunt.flowandroid.FragmentGroupFlowController

class MainActivity : FlowActivity() {
    override fun getInitialFlow(): FragmentGroupFlowController = FragmentGroupFlowController(R.layout.group_single)

    override fun getInitialArgs(): FragmentGroupFlowController.FlowsInGroup = FragmentGroupFlowController.FlowsInGroup(
        mapOf(
            R.id.singleLayout to RootFlowController::class.java as Class<FragmentFlowController<Unit, Unit>>
//            R.id.bottomLayout to RootFlowController::class.java as Class<FragmentFlowController<Unit, Unit>>
        )
    )
}
