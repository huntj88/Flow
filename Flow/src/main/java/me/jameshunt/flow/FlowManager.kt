package me.jameshunt.flow

object FlowManager {

    private var _rootFlow: FlowController<Unit, Unit>? = null

    val shouldResume: Boolean
        get() = this._rootFlow != null

    fun launchFlow(getInitialFlow: () -> FlowController<Unit,Unit>, onFlowFinished: () -> Unit) {
        this._rootFlow = getInitialFlow()

        this._rootFlow!!.launchFlow(Unit)
            .catch { it.printStackTrace() }
            .always {
                this._rootFlow = null
                onFlowFinished()
                println("flow completed")
            }
    }
}