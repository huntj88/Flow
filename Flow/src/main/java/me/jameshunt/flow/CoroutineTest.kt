package me.jameshunt.flow

import kotlinx.coroutines.*

fun main() = runBlocking {
    CoroutineTest().ee()
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
}