package me.jameshunt.flow

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select
import java.util.concurrent.CopyOnWriteArraySet

// todo: check?
fun Throwable.isExpectedErrorOrThrow() {
    this.message?.contains("Can not perform this action after onSaveInstanceState")
        ?: this.message?.contains("View does not exist for fragment") ?: throw this
}

suspend fun <T> List<Deferred<T>>.awaitFirst(): T {
    require(1 <= size)

    val toAwait = CopyOnWriteArraySet<Deferred<T>>(this)
    val result = ArrayList<T>()

    suspend fun whileSelect(builder: SelectBuilder<Boolean>.() -> Unit) {
        while (select<Boolean>(builder)) {
        }
    }

    whileSelect {
        toAwait.forEach { deferred ->
            deferred.onAwait {
                toAwait.remove(deferred)
                toAwait.forEach { it.cancel() }
                result.add(it)
                result.size != 1
            }
        }
    }

    assert(result.size == 1)
    return result.first()
}