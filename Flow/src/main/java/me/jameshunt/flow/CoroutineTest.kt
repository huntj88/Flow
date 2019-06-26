package me.jameshunt.flow

import kotlinx.coroutines.*

fun main() = runBlocking {
    //    try {
//        CoroutineTest().preThrow().await()
//    } catch (e: IllegalStateException) {
//        println("caught")
//    }

    DeferredTest().doTheThing()
}

class DeferredTest {

    suspend fun doTheThing() {
        val deferred = CompletableDeferred<Unit>()

        coroutineScope {
            listOf(
                async {
                    delay(2000)
                    try {
                        throw IllegalStateException()
                    } catch (e: IllegalStateException) {
                        deferred.completeExceptionally(IllegalStateException())
                    }
                },
                async { deferred.waitEx() }
            ).awaitAll()
        }
    }

    suspend fun Deferred<Unit>.waitEx() {
        try {
            this.await()
            println("wow")
        } catch (e: IllegalStateException) {
            println("ugh")
        }
    }
}

class CoroutineTest {

    suspend fun blah() {
        delay(1000)
        //withContext(Dispatchers.Default) {
        //Thread.sleep(1000)
        println("hello")
        //}
    }

    suspend fun ee() {
        coroutineScope {
            listOf(
                async { blah() },
                async { blah() }
            ).awaitAll()

            blah()
        }
    }

    suspend fun preThrow(): Deferred<Unit> {
//        return coroutineScope {
//            async { throwError() }.await()
//        }

        return throwError()
    }

    suspend fun throwError(): Deferred<Unit> {
        val completableDeferred = CompletableDeferred<Unit>()

        completableDeferred.completeExceptionally(IllegalStateException())

        return completableDeferred
    }
}