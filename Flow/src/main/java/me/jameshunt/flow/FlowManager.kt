package me.jameshunt.flow

object FlowManager {

    private var _rootFlow: FlowController<*, Unit>? = null

    val shouldResume: Boolean
        get() = this._rootFlow != null

    fun <Input> launchFlow(getInitialFlow: () -> FlowController<Input, Unit>, args: Input, onFlowFinished: () -> Unit) {
        this._rootFlow = getInitialFlow().also { rootFlow ->
            rootFlow
                .launchFlow(args)
                .catch { it.printStackTrace() }
                .always {
                    this._rootFlow = null
                    onFlowFinished()
                    println("flow completed")
                }
        }
    }
}