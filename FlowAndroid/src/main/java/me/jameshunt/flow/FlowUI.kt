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

    override fun onResume() {
        super.onResume()

        // proxy should be optional unlike normal fragment
        proxy?.input?.let {
            this.flowWillRun(it)
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

        // DialogFragments are kept around in FragmentManager memory longer,
        // don't let it resolve if fragment is from old activity
        if(proxy?.fragment?.get() == this) {
            proxy?.onBack()
        }
    }
}