package me.jameshunt.flow3.root2

import me.jameshunt.flow.*
import me.jameshunt.flow.promise.Promise
import me.jameshunt.flow3.TestFragment
import me.jameshunt.flow3.root2.GeneratedRootFlow.RootFlowState.*

class RootFlowController(viewId: ViewId) : GeneratedRootFlow<Unit, Unit>(viewId) {

    private val testFragmentProxy = proxy(TestFragment::class.java)

    override fun onStart(state: InitialState<Unit>) {
        TODO()
    }

    override fun onOne(state: One): Promise<FromOne> {
        return this.flow(this.testFragmentProxy, "wow").forResult<Unit, FromOne>(
            onBack = { Promise(Three("wow")) },
            onComplete = {
                // wrapper in a Promise in case you need to do some async stuff
                Promise(Four("complete"))
            },
            onCatch = { throw it }
        )
    }

    override fun onTwo(state: Two): Promise<FromTwo> {
        TODO()
    }

    override fun onThree(state: Three): Promise<FromThree> {
        TODO()
    }

    override fun onFour(state: Four): Promise<FromFour> {
        TODO()
    }
}

