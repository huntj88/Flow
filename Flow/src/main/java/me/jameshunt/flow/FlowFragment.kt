package me.jameshunt.flow

import androidx.fragment.app.Fragment
import me.jameshunt.flow.promise.Promise

abstract class FlowFragment<Input, Output> : Fragment() {

    var proxy: FragmentProxy<Input, Output, *>? = null

    private var input: Input? = null

    fun flowForResult(input: Input): Promise<FlowResult<Output>> {

        this.input = input

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
        this.input?.let {
            this.flowWillRun(it)
        }
    }
}
