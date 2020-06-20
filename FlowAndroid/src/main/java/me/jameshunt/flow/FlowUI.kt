package me.jameshunt.flow

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.inmotionsoftware.promisekt.Promise

interface FlowUI<Input, Output> {
    var proxy: FragmentProxy<Input, Output, *>?

    fun flowForResult(): Promise<FlowResult<Output>>
}

abstract class FlowFragment<Input, Output> : Fragment(), FlowUI<Input, Output> {

    override var proxy: FragmentProxy<Input, Output, *>? = null

    final override fun flowForResult(): Promise<FlowResult<Output>> {
        val input = proxy!!.input as Input

        this.view?.let {
            this.flowWillRun(input)
        }

        return proxy!!.deferredPromise.promise
    }

    abstract fun flowWillRun(input: Input)

    fun resolve(output: Output) {
        this.proxy!!.resolve(output)
    }

    fun fail(error: Throwable) {
        this.proxy!!.fail(error)
    }

    override fun onResume() {
        super.onResume()
        proxy!!.input?.let {
            this.flowWillRun(it)
        }
    }
}

abstract class FlowDialogFragment<Input, Output> : DialogFragment(), FlowUI<Input, Output> {
    override var proxy: FragmentProxy<Input, Output, *>? = null

    final override fun flowForResult(): Promise<FlowResult<Output>> {
        val input = proxy!!.input as Input

        this.view?.let {
            this.flowWillRun(input)
        }

        return proxy!!.deferredPromise.promise
    }

    abstract fun flowWillRun(input: Input)

    fun resolve(output: Output) {
        this.proxy!!.resolve(output)
        this.dismiss()
    }

    fun fail(error: Throwable) {
        this.proxy!!.fail(error)
        this.dismiss()
    }

    override fun onResume() {
        super.onResume()

        // proxy should be optional unlike normal fragment
        proxy?.input?.let {
            this.flowWillRun(it)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        // is state is saved then dialog is triggered from a config change and will be recreated
        // is state is not saved then dialog is closing, and back should be called
        if(!isStateSaved) {
            proxy?.back()
        }
    }
}
