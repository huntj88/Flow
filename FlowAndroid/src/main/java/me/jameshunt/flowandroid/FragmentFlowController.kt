package me.jameshunt.flowandroid

import me.jameshunt.flow.FlowController
import me.jameshunt.flow.Promise

typealias ViewId = Int

abstract class FragmentFlowController<Input, Output>(private val viewId: ViewId) : FlowController<Input, Output>() {

    protected fun <NewInput, NewOutput> flow(
        controller: Class<FragmentFlowController<NewInput, NewOutput>>,
        viewId: ViewId,
        arg: NewInput
    ): Promise<NewOutput> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(arg)
    }

    fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        arg: FragInput
    ): Promise<FragOutput> {
        val displayManager = AndroidFlowManager.fragmentDisplayManager.get()
            ?: throw IllegalStateException("Should never happen")

        return displayManager
            .show(fragmentProxy = fragmentProxy, viewId = this.viewId)
            .flowForResult(arg)
    }
}