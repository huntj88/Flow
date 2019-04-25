package me.jameshunt.flowandroid

import androidx.fragment.app.Fragment
import me.jameshunt.flow.DeferredPromise
import me.jameshunt.flow.FlowResult
import java.lang.ref.WeakReference
import java.util.*

class FragmentProxy<FragInput, FragOutput, FragmentType : FlowFragment<FragInput, FragOutput>>(
    internal val clazz: Class<FragmentType>
) {

    internal lateinit var fragment: WeakReference<FragmentType>

    private var state: Fragment.SavedState? = null

    internal val tag = UUID.randomUUID().toString()

    internal var deferredPromise = DeferredPromise<FlowResult<FragOutput>>()
        private set

    internal fun bind(fragment: FragmentType) {
        this.restoreState(fragment)
        this.deferredPromise = DeferredPromise()
        this.fragment = WeakReference(fragment)
        fragment.proxy = this
    }

    internal fun saveState() {
        this.fragment.get()?.let {
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

    internal fun resolve(arg: FragOutput) {
        this.deferredPromise.resolve(FlowResult.Completed(arg))
    }
}