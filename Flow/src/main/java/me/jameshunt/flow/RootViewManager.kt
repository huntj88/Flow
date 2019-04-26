package me.jameshunt.flow

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout

typealias LayoutId = Int

class RootViewManager(private val activity: FlowActivity<*>) {

    fun setNewRoot(layoutId: LayoutId): ViewGroup {
        val flowRootLayout = activity.findViewById<FrameLayout>(R.id.flowRootLayout)
        flowRootLayout.removeAllViews()
        return LayoutInflater.from(activity).inflate(layoutId, flowRootLayout) as ViewGroup
    }
}