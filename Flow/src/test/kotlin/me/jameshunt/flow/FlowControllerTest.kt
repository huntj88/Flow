package me.jameshunt.flow

import com.inmotionsoftware.promisekt.PMKConfiguration
import com.inmotionsoftware.promisekt.catch
import com.inmotionsoftware.promisekt.conf
import com.inmotionsoftware.promisekt.done
import org.junit.Test

import org.junit.Assert.*

class FlowControllerTest {

    init {
        conf.Q = PMKConfiguration.Value(null, null)
    }

    @Test
    fun testResolveDone() {
        val testFlow = object : FlowController<Unit, Unit>() {
            override fun onStart(state: InitialState<Unit>) {
                onDone(Unit)
            }
        }

        testFlow
            .launchFlow(Unit)
            .done { assertTrue(true) }
            .catch { fail(it.toString()) }
    }

    @Test
    fun testResolveCatch() {
        val testFlow = object : FlowController<Unit, Unit>() {
            override fun onStart(state: InitialState<Unit>) {
                onCatch(IllegalStateException("OH NO"))
            }
        }

        testFlow
            .launchFlow(Unit)
            .done { fail() }
            .catch { assertTrue(true) }
    }

    @Test
    fun testFlowTransform() {
        val testFlow = object : FlowController<Int, Int>() {
            override fun onStart(state: InitialState<Int>) {
                onDone(state.input * 2)
            }
        }

        testFlow
            .launchFlow(2)
            .done { assertEquals("double the input", 4, it) }
            .catch { fail() }
    }
}