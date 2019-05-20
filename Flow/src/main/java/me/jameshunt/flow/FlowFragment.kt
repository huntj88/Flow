package me.jameshunt.flow

import androidx.fragment.app.Fragment
import me.jameshunt.flow.promise.Promise

abstract class FlowFragment<Input, Output> : Fragment() {

    var proxy: FragmentProxy<Input, Output, *>? = null

    fun flowForResult(): Promise<FlowResult<Output>> {
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
