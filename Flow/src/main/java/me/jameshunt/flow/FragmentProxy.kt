package me.jameshunt.flow

import androidx.fragment.app.Fragment
import me.jameshunt.flow.promise.DeferredPromise
import java.lang.ref.WeakReference
import java.util.*

fun <FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>> FragmentFlowController<*, *>
        .proxy(clazz: Class<FragmentType>): FragmentProxy<FragInput, FragOutput, FragmentType> = FragmentProxy(clazz)

class FragmentProxy<FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>>(
    internal val clazz: Class<FragmentType>
) {

    internal var fragment: WeakReference<FragmentType>? = null

    private var state: Fragment.SavedState? = null

    internal val tag = UUID.randomUUID().toString()

    internal var deferredPromise = DeferredPromise<FlowResult<FragOutput>>()
        private set

    internal fun bind(fragment: FragmentType) {
        this.restoreState(fragment)
        this.deferredPromise = if(this.deferredPromise.promise.isPending) this.deferredPromise else DeferredPromise()
        this.fragment = WeakReference(fragment)
        fragment.proxy = this
    }

    internal fun saveState() {
        this.fragment?.get()?.let {
            // We can't save the state of a Fragment that isn't added to a FragmentManager.
            if (it.isAdded) {
                this.state = it.fragmentManager?.saveFragmentInstanceState(it)
            }
        }
    }

    private fun restoreState(fragment: FragmentType) {
        this.state?.let {
            // Can't set initial state if already added
            if (!fragment.isAdded) {
                fragment.setInitialSavedState(this.state)
            }
        }
    }

    internal fun onBack() {
        this.deferredPromise.resolve(FlowResult.Back)
    }

    internal fun resolve(output: FragOutput) {
        this.deferredPromise.resolve(FlowResult.Completed(output))
    }
}