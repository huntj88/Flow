package me.jameshunt.flow

import com.inmotionsoftware.promisekt.PMKConfiguration
import com.inmotionsoftware.promisekt.catch
import com.inmotionsoftware.promisekt.conf
import com.inmotionsoftware.promisekt.done
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

class FlowControllerTest {

    init {
        conf.Q = PMKConfiguration.Value(null, null)
    }

    @Test
    fun testResolveDone() {
        val testFlow = object : FlowController<Unit, Unit>() {
            override suspend fun onStart(state: InitialState<Unit>) {
                onDone(Unit)
            }
        }

        runBlocking {
            testFlow.launchFlow(Unit)
            assertTrue(true)
        }
    }

    @Test
    fun testResolveCatch() {
        val testFlow = object : FlowController<Unit, Unit>() {
            override suspend fun onStart(state: InitialState<Unit>) {
                onCatch(IllegalStateException("OH NO"))
            }
        }

        runBlocking {
            try {
                testFlow.launchFlow(Unit)
                fail()
            } catch (e: IllegalStateException) {
                assertTrue(true)
            }
        }
    }

    @Test
    fun testFlowTransform() {
        val testFlow = object : FlowController<Int, Int>() {
            override suspend fun onStart(state: InitialState<Int>) {
                onDone(state.input * 2)
            }
        }

        runBlocking {
            val output = testFlow.launchFlow(2)
            assertEquals("double the input", 4, output)
        }
    }
}