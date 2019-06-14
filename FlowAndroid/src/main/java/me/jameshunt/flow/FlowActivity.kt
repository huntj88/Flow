package me.jameshunt.flow

import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity

data class DeepLinkData(val intentBundle: Bundle?)

abstract class FlowActivity<RootFlowController : FragmentFlowController<DeepLinkData, Unit>> : AppCompatActivity() {

    internal val rootViewManager: RootViewManager by lazy { RootViewManager(this) }
    internal val fragmentDisplayManager = FragmentDisplayManager(this.supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_flow)

//        this.intent.putExtra("test", "deepLinkData")

        FlowManager.launchFlow(flowActivity = this)
    }

    override fun onResume() {
        super.onResume()
        FlowManager.retryUncommittedFragmentTransactions()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        this.fragmentDisplayManager.saveAll()
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ACTIVITY_FOR_RESULT) {
            FlowManager.activityForResultManager.onActivityResult(data)
        }
    }

    @CallSuper
    override fun onBackPressed() {
        FlowManager.delegateBack()
    }

    internal fun onFlowFinished() {
        super.onBackPressed()
    }

    abstract fun getInitialFlow(): Class<RootFlowController>

    fun getInitialGroupFlow(): SimpleGroupController<DeepLinkData, Unit> = SimpleGroupController()

    internal fun getInitialArgs(): SimpleGroupController.SimpleGroupInput<DeepLinkData, Unit> {
        return SimpleGroupController.SimpleGroupInput(
            flow = getInitialFlow() as Class<FragmentFlowController<DeepLinkData, Unit>>,
            input = DeepLinkData(this.intent?.extras).also {
                // consume the deepLink data,
                this.intent.extras?.clear()
            }
        )
    }
}