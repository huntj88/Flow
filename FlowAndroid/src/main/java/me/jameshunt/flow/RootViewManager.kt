package me.jameshunt.flow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

typealias LayoutId = Int

internal class RootViewManager(private val activity: FlowActivity<*>) {

    fun setNewRoot(layoutId: LayoutId): ViewGroup {
        val flowRootLayout = activity.findViewById<FrameLayout>(R.id.flowRootLayout)
        flowRootLayout.removeAllViews()

        val layout = LayoutInflater.from(activity).inflate(layoutId, flowRootLayout, false) as ViewGroup
        flowRootLayout.addView(layout)
        return layout
    }

    fun isViewVisible(viewId: ViewId): Boolean = activity.findViewById<View?>(viewId) != null
}