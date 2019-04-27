package me.jameshunt.flow3.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import me.jameshunt.flow.FlowFragment

class SplashFragment: FlowFragment<Unit, Unit>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ProgressBar(context)
    }

    override fun flowWillRun(input: Unit) {
        this.resolve(Unit)
    }
}