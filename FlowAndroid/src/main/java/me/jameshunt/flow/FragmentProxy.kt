package me.jameshunt.flow

import androidx.fragment.app.Fragment
import com.inmotionsoftware.promisekt.DeferredPromise
import com.inmotionsoftware.promisekt.isPending
import java.lang.ref.WeakReference
import java.util.UUID

fun <FragInput, FragOutput, FragmentType : FlowUI<FragInput, FragOutput>> FragmentFlowController<*, *>
        .proxy(clazz: Class<FragmentType>): FragmentProxy<FragInput, FragOutput, FragmentType> = FragmentProxy(clazz)

class FragmentProxy<FragInput, FragOutput, FragmentType : FlowUI<FragInput, FragOutput>>(
    internal val clazz: Class<FragmentType>
) {

    internal var fragment: WeakReference<FragmentType>? = null

    private var state: Fragment.SavedState? = null

    internal val tag = UUID.randomUUID().toString()

    internal var input: FragInput? = null

    internal var deferredPromise: DeferredPromise<FlowResult<FragOutput>> = DeferredPromise()
        get() {
            field = if (field.promise.isPending) field else DeferredPromise()
            return field
        }

    internal fun bind(fragment: FragmentType) {
        this.restoreState(fragment)
        this.fragment = WeakReference(fragment)
        fragment.proxy = this
    }

    internal fun saveState() {
        this.fragment?.get()?.let {
            it as Fragment
            // We can't save the state of a Fragment that isn't added to a FragmentManager.
            if (it.isAdded) {
                this.state = it.fragmentManager?.saveFragmentInstanceState(it)
            }
        }
    }

    private fun restoreState(fragment: FragmentType) {
        this.state?.let {
            fragment as Fragment
            // Can't set initial state if already added
            if (!fragment.isAdded) {
                fragment.setInitialSavedState(this.state)
            }
        }
    }

    internal fun resolve(output: FragOutput) {
        this.deferredPromise.resolve(FlowResult.Completed(output))
    }

    internal fun back() {
        this.deferredPromise.resolve(FlowResult.Back)
    }

    internal fun fail(error: Throwable) {
        this.deferredPromise.reject(error)
    }
}