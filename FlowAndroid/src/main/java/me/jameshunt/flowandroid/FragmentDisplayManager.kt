package me.jameshunt.flowandroid

import androidx.fragment.app.FragmentManager

class FragmentDisplayManager(private val fragmentManager: FragmentManager) {

    fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> show(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>, viewId: ViewId
    ): FragmentType {
        (fragmentManager.findFragmentById(viewId) as? FlowFragment<*,*>)?.proxy?.saveState()

        val fragment = fragmentProxy.clazz.newInstance()
            .also { fragmentProxy.bind(it) }

        fragmentManager.beginTransaction().replace(viewId, fragment, fragmentProxy.tag).commit()
        return fragment
    }

    fun saveAll() {
        fragmentManager.fragments.forEach {
            (it as FlowFragment<*,*>).proxy.saveState()
        }
    }

    fun remove(activeFragment: FragmentProxy<*, *, *>?) {
        fragmentManager.beginTransaction().remove(activeFragment?.fragment?.get()!!).commit()
    }

    fun removeAll() {
        fragmentManager.beginTransaction().also { transaction ->
            fragmentManager.fragments.forEach {
                transaction.remove(it)
            }
        }.commit()
    }
}