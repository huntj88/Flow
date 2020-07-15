package me.jameshunt.flow

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

interface FlowUI<Input, Output> {
    var proxy: FragmentProxy<Input, Output, *>?

    suspend fun flowForResult(): FlowResult<Output>
    fun getAndConsumeInputData(): FlowUIInput<Input>
}

sealed class FlowUIInput<out Input> {
    data class NewData<Input>(val data: Input): FlowUIInput<Input>()
    object ResumeSavedState: FlowUIInput<Nothing>()
}

abstract class FlowFragment<Input, Output> : Fragment(), FlowUI<Input, Output> {

    override var proxy: FragmentProxy<Input, Output, *>? = null

    private var newInput = false

    final override suspend fun flowForResult(): FlowResult<Output> {
        newInput = true
        return proxy!!.deferredPromise.await()
    }

    override fun getAndConsumeInputData(): FlowUIInput<Input> {
        return when(newInput) {
            true -> FlowUIInput.NewData(proxy!!.input) as FlowUIInput<Input>
            false -> FlowUIInput.ResumeSavedState
        }.also { newInput = false }
    }

    fun resolve(output: Output) {
        this.proxy!!.resolve(output)
    }

    fun fail(error: Throwable) {
        this.proxy!!.fail(error)
    }
}

abstract class FlowDialogFragment<Input, Output> : DialogFragment(), FlowUI<Input, Output> {
    override var proxy: FragmentProxy<Input, Output, *>? = null

    private var newInput = false

    final override suspend fun flowForResult(): FlowResult<Output> {
        newInput = true
        return proxy!!.deferredPromise.await()
    }

    override fun getAndConsumeInputData(): FlowUIInput<Input> {
        return when(newInput) {
            true -> FlowUIInput.NewData(proxy!!.input) as FlowUIInput<Input>
            false -> FlowUIInput.ResumeSavedState
        }.also { newInput = false }
    }

    fun resolve(output: Output) {
        this.proxy!!.resolve(output)
        this.dismiss()
    }

    fun fail(error: Throwable) {
        this.proxy!!.fail(error)
        this.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        // DialogFragments are kept around in FragmentManager memory longer,
        // don't let it resolve if fragment is from old activity
        if(proxy?.fragment?.get() == this) {
            proxy?.back()
        }
    }
}
