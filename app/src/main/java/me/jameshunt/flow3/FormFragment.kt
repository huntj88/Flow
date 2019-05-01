package me.jameshunt.flow3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_form.*
import me.jameshunt.flow.FlowFragment

class FormFragment: FlowFragment<Unit, Unit>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_form, container, false)
    }

    override fun flowWillRun(input: Unit) {
        this.button.setOnClickListener {
            this.resolve(Unit)
        }
    }
}