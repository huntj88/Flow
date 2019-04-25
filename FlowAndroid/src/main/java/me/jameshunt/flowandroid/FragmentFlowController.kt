package me.jameshunt.flowandroid

import me.jameshunt.flow.FlowController
import me.jameshunt.flow.FlowResult
import me.jameshunt.flow.Promise
import me.jameshunt.flow.always

typealias ViewId = Int

abstract class FragmentFlowController<Input, Output>(private val viewId: ViewId) : FlowController<Input, Output>() {

    private var activeFragment: FragmentProxy<*, *, *>? = null

    protected fun <NewInput, NewOutput> flow(
        controller: Class<FragmentFlowController<NewInput, NewOutput>>,
        viewId: ViewId,
        arg: NewInput
    ): Promise<FlowResult<NewOutput>> {
        val flowController = controller
            .getDeclaredConstructor(ViewId::class.java)
            .newInstance(viewId)

        childFlows.add(flowController)

        return flowController.launchFlow(arg)
    }

    fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        arg: FragInput
    ): Promise<FlowResult<FragOutput>> {
        val displayManager = AndroidFlowManager.fragmentDisplayManager.get()
            ?: throw IllegalStateException("Should never happen")

        return displayManager
            .show(fragmentProxy = fragmentProxy, viewId = this.viewId)
            .also { this.activeFragment = fragmentProxy }
            .flowForResult(arg)
            .always { this.activeFragment = null }
    }

    override fun onDone(arg: Output) {
        val displayManager = AndroidFlowManager.fragmentDisplayManager.get()
            ?: throw IllegalStateException("Should never happen")

        displayManager.remove(activeFragment)
        super.onDone(arg)
    }

    override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.activeFragment?.onBack()
    }
}