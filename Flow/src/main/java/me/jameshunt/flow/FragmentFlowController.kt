package me.jameshunt.flow

import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.always

abstract class FragmentFlowController<Input, Output>(private val viewId: ViewId) : FlowController<Input, Output>() {

    private var activeFragment: FragmentProxy<*, *, *>? = null

    fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        arg: FragInput
    ): Promise<FlowResult<FragOutput>> {
        val displayManager = FlowManager.fragmentDisplayManager.get()
            ?: throw IllegalStateException("Should never happen")

        return displayManager
            .show(fragmentProxy = fragmentProxy, viewId = this.viewId)
            .also { this.activeFragment = fragmentProxy }
            .flowForResult(arg)
            .always { this.activeFragment = null }
    }

    override fun onDone(arg: Output) {
        val displayManager = FlowManager.fragmentDisplayManager.get()
            ?: throw IllegalStateException("Should never happen")

        displayManager.remove(activeFragment)
        super.onDone(arg)
    }

    override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.activeFragment?.onBack()
    }
}