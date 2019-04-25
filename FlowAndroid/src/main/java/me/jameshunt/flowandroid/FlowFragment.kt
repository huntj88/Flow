package me.jameshunt.flowandroid

import androidx.fragment.app.Fragment
import me.jameshunt.flow.Promise

abstract class FlowFragment<Input, Output> : Fragment() {

    lateinit var proxy: FragmentProxy<Input, Output, *>

    private var args: Input? = null

    fun flowForResult(args: Input): Promise<Output> {

        this.args = args

        this.view?.let {
            this.flowWillRun(args)
        }

        return proxy.deferredPromise.promise
    }

    abstract fun flowWillRun(args: Input)

    fun resolve(output: Output) {
        this.proxy.resolve(output)
    }

    override fun onResume() {
        super.onResume()
        this.args?.let {
            this.flowWillRun(it)
        }
    }
}
