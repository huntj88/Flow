package me.jameshunt.flow

import android.view.ViewGroup
import me.jameshunt.flow.promise.always

class SimpleGroupController: FragmentGroupFlowController<Unit>(R.layout.group_simple) {
    override fun setupGroup(layout: ViewGroup, flowsInGroup: FlowsInGroup<Unit>) {
        flowsInGroup.map.forEach { (viewId, flowController) ->
            this.flow(controller = flowController, viewId = viewId, arg = Unit).always {
                this.onBack()
            }
        }
    }
}