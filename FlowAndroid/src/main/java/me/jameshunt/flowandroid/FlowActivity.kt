package me.jameshunt.flowandroid

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

abstract class FlowActivity : AppCompatActivity() {

    private val rootViewManager: RootViewManager by lazy { RootViewManager(this) }
    private val fragmentDisplayManager = FragmentDisplayManager(this.supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_flow)

        AndroidFlowManager.rootViewManager = WeakReference(this.rootViewManager)
        AndroidFlowManager.fragmentDisplayManager = WeakReference(this.fragmentDisplayManager)

        AndroidFlowManager.launchFlow(flowActivity = this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        this.fragmentDisplayManager.onSave()
        super.onSaveInstanceState(outState)
    }

    @CallSuper
    override fun onBackPressed() {
//        FlowManager.delegateBack()
    }

    internal fun onFlowFinished() {
        super.onBackPressed()
    }

    abstract fun getInitialFlow(): FragmentGroupFlowController
    abstract fun getInitialArgs(): FragmentGroupFlowController.FlowsInGroup
}