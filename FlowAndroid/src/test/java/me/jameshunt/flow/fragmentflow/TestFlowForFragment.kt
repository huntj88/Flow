package me.jameshunt.flow.fragmentflow

import android.os.Bundle
import me.jameshunt.flow.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TestFlowForFragment {

    private lateinit var activity: FlowActivity<*>

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(ActivityTestImpl::class.java)
            .create()
            .recreate()
            .pause()
            .resume()
            .pause()
            .stop()
            .recreate()
            .get()
    }

    @Test
    fun `test adding single fragment`() {
        val fragments = activity.supportFragmentManager.fragments
        Assert.assertEquals(1, fragments.size)
        Assert.assertTrue(fragments.first().isAdded)
    }
}

class ActivityTestImpl : FlowActivity<TestAddFragmentFlow>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_AppCompat);
        super.onCreate(savedInstanceState)
    }

    override fun getInitialFlow(): Class<TestAddFragmentFlow> = TestAddFragmentFlow::class.java
}

class TestAddFragmentFlow : FragmentFlowController<DeepLinkData, Unit>() {
    override fun onStart(state: InitialState<DeepLinkData>) {
        this.flow(proxy(TestFragment::class.java), Unit)
    }
}

class TestFragment : FlowFragment<Unit, Unit>() {
    override fun flowWillRun(input: Unit) {

    }
}