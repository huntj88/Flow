package me.jameshunt.flowandroid

import android.view.LayoutInflater
import android.widget.FrameLayout

typealias LayoutId = Int

class RootViewManager(private val activity: FlowActivity) {

    fun setNewRoot(layoutId: LayoutId) {
        val flowRootLayout = activity.findViewById<FrameLayout>(R.id.flowRootLayout)
        flowRootLayout.removeAllViews()
        LayoutInflater.from(activity).inflate(layoutId, flowRootLayout)
    }
}