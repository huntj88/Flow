package me.jameshunt.flow

import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity

data class DeepLinkData(val intent: Intent?)

abstract class FlowActivity<RootFlowController: FragmentFlowController<DeepLinkData, Unit>> : AppCompatActivity() {

    internal val rootViewManager: RootViewManager by lazy { RootViewManager(this) }
    internal val fragmentDisplayManager = FragmentDisplayManager(this.supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_flow)

        FlowManager.launchFlow(flowActivity = this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        this.fragmentDisplayManager.saveAll()
        super.onSaveInstanceState(outState)
    }

    @CallSuper
    override fun onBackPressed() {
        FlowManager.delegateBack()
    }

    internal fun onFlowFinished() {
        super.onBackPressed()
    }

    abstract fun getInitialFlow(): Class<RootFlowController>

    fun getInitialGroupFlow(): DeepLinkGroupController = DeepLinkGroupController(DeepLinkData(this.intent))
    fun getInitialArgs(): FragmentGroupFlowController.FlowsInGroup<Unit> = FragmentGroupFlowController.FlowsInGroup(
        map = mapOf(R.id.groupSimple to getInitialFlow() as Class<FragmentFlowController<*, *>>),
        extra = Unit
    )
}