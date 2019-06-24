package me.jameshunt.flow

import com.inmotionsoftware.promisekt.PMKConfiguration
import com.inmotionsoftware.promisekt.catch
import com.inmotionsoftware.promisekt.conf
import com.inmotionsoftware.promisekt.done
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BusinessFlowControllerTest {

    init {
        conf.Q = PMKConfiguration.Value(null, null)
    }

    @Test
    fun `test flow(controller, input)`() {
        runBlocking {
            FlowOne().launchFlow(Unit)
            assertTrue(true)
        }
    }
}

private class FlowOne : BusinessFlowController<Unit, Unit>() {
    override suspend fun onStart(state: InitialState<Unit>) {
        this@FlowOne.flow(FlowTwo::class.java, Unit)
        this@FlowOne.onDone(Unit)
    }
}

private class FlowTwo : BusinessFlowController<Unit, Unit>() {
    override suspend fun onStart(state: InitialState<Unit>) {
        this.onDone(Unit)
    }
}