package me.jameshunt.flow

import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch

fun main() {
    val countDownLatch = CountDownLatch(1)

    GlobalScope.launch {
        CoroutineTest().ee()
        countDownLatch.countDown()
    }

    countDownLatch.await()
}

class CoroutineTest {

    suspend fun blah() {
        delay(3000)
        println("hello")
    }

    suspend fun ee() {
        coroutineScope {
            listOf(
                async { blah() },
                async { blah() }
            ).awaitAll()

            withContext(Dispatchers.Default) {
                blah()
            }
        }
    }
}