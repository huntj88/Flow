package me.jameshunt.flowandroid

import androidx.fragment.app.FragmentManager

class FragmentDisplayManager(private val fragmentManager: FragmentManager) {

    fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> show(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>, args: FragInput, viewId: ViewId
    ): FragmentType {
        val fragment = fragmentProxy.clazz.newInstance().also { fragmentProxy.bind(it) }
        fragmentManager.beginTransaction().replace(viewId, fragment).commit()
        return fragment
    }
}