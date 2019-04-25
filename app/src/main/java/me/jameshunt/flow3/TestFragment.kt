package me.jameshunt.flow3

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.jameshunt.flowandroid.FlowFragment

class TestFragment: FlowFragment<String, Unit>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return TextView(context).apply {
            setOnClickListener {
                this@TestFragment.resolve(Unit)
            }
        }
    }

    override fun flowWillRun(arg: String) {
        (this.view as TextView).text = arg
    }
}