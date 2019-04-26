package me.jameshunt.flow

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

abstract class FlowActivity<RootFlowController: FragmentFlowController<Unit, Unit>> : AppCompatActivity() {

    private val rootViewManager: RootViewManager by lazy { RootViewManager(this) }
    private val fragmentDisplayManager = FragmentDisplayManager(this.supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_flow)

        FlowManager.rootViewManager = WeakReference(this.rootViewManager)
        FlowManager.fragmentDisplayManager = WeakReference(this.fragmentDisplayManager)

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
    fun getInitialArgs(): FragmentGroupFlowController.FlowsInGroup<Unit> = FragmentGroupFlowController.FlowsInGroup(
        map = mapOf(R.id.groupSimple to getInitialFlow() as Class<FragmentFlowController<Unit, Unit>>),
        extra = Unit
    )
}