package me.jameshunt.flow

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity

data class DeepLinkData(val intentBundle: Bundle?)

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

    fun getInitialGroupFlow(): SimpleGroupController = SimpleGroupController()

    internal fun getInitialArgs(): FragmentGroupFlowController.DeepLinkFlowGroup = FragmentGroupFlowController.DeepLinkFlowGroup(
        viewId = R.id.groupSimple,
        deepLinkFlow = getInitialFlow() as Class<FragmentFlowController<DeepLinkData, Unit>>,
        deepLinkData = DeepLinkData(this.intent?.extras)
    )
}