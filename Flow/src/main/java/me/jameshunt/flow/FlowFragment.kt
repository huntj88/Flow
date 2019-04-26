package me.jameshunt.flow

import androidx.fragment.app.Fragment
import me.jameshunt.flow.promise.Promise

abstract class FlowFragment<Input, Output> : Fragment() {

    lateinit var proxy: FragmentProxy<Input, Output, *>

    private var arg: Input? = null

    fun flowForResult(arg: Input): Promise<FlowResult<Output>> {

        this.arg = arg

        this.view?.let {
            this.flowWillRun(arg)
        }

        return proxy.deferredPromise.promise
    }

    abstract fun flowWillRun(arg: Input)

    fun resolve(output: Output) {
        this.proxy.resolve(output)
    }

    override fun onResume() {
        super.onResume()
        this.arg?.let {
            this.flowWillRun(it)
        }
    }
}
