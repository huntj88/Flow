package me.jameshunt.flow

import com.inmotionsoftware.promisekt.PMKConfiguration
import com.inmotionsoftware.promisekt.catch
import com.inmotionsoftware.promisekt.conf
import com.inmotionsoftware.promisekt.done
import org.junit.Assert.*
import org.junit.Test

class BusinessFlowControllerTest {

    init {
        conf.Q = PMKConfiguration.Value(null, null)
    }

    @Test
    fun `test flow(controller, input)`() {
        FlowOne().launchFlow(Unit)
            .done { assertTrue(true) }
            .catch { fail() }
    }
}

private class FlowOne : BusinessFlowController<Unit, Unit>() {
    override fun onStart(state: InitialState<Unit>) {
        this.flow(FlowTwo::class.java, Unit)
            .done { this.onDone(Unit) }
            .catch { this.onCatch(it) }
    }
}

private class FlowTwo : BusinessFlowController<Unit, Unit>() {
    override fun onStart(state: InitialState<Unit>) {
        this.onDone(Unit)
    }
}