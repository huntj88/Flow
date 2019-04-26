package me.jameshunt.flow

import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow.promise.catch
import me.jameshunt.flow.promise.doAlso

sealed class FlowResult<out Type> {
    data class Completed<Data>(val data: Data) : FlowResult<Data>()
    object Back: FlowResult<Nothing>()
}

inline fun <T> Promise<FlowResult<T>>.complete(crossinline onComplete: (T) -> Unit): Promise<FlowResult<T>> {
    return this.doAlso { result ->
        (result as? FlowResult.Completed)?.let {
            onComplete(it.data)
        }
    }.catch {
        it.printStackTrace()
        throw it
    }
}

inline fun <T> Promise<FlowResult<T>>.back(crossinline onBack: () -> Unit): Promise<FlowResult<T>> {
    return this.doAlso { result ->
        (result as? FlowResult.Back)?.also {
            onBack()
        }
    }
}
