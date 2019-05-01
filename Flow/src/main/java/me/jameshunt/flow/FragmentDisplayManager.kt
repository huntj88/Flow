package me.jameshunt.flow

import androidx.fragment.app.FragmentManager
import me.jameshunt.flow.util.logFlow

class FragmentDisplayManager(private val fragmentManager: FragmentManager) {

    fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> show(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>, viewId: ViewId
    ): FragmentType {
        (fragmentManager.findFragmentById(viewId) as? FlowFragment<*, *>)?.proxy?.saveState()

        val fragment = fragmentProxy.fragment?.get() ?: fragmentProxy.clazz.newInstance()

        fragmentProxy.bind(fragment)

        fragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom)
            .replace(viewId, fragment, fragmentProxy.tag)
            .commit()

        return fragment
    }

    fun saveAll() {
        fragmentManager.fragments.forEach {
            (it as FlowFragment<*, *>).proxy!!.saveState()
        }
    }

    fun remove(activeFragment: FragmentProxy<*, *, *>?) {
        activeFragment?.fragment?.get()
            ?.let { fragmentManager.beginTransaction().remove(it).commit() }
            ?: logFlow("no active fragment")

    }

    fun removeAll(blocking: Boolean = false) {
        fragmentManager.beginTransaction()
            .also { transaction ->
                fragmentManager.fragments.forEach {
                    transaction.remove(it)
                }
            }
            .let {
                when (blocking) {
                    true -> it.commitNow()
                    false -> it.commit()
                }
            }
    }
}