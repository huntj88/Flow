package me.jameshunt.flow.promise

import android.os.Handler
import android.os.Looper
import com.inmotionsoftware.promisekt.PMKConfiguration
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object DispatchExecutor {
    val main: Executor by lazy { Executor { Handler(Looper.getMainLooper()).post(it) } }
    val background: Executor by lazy { Executors.newCachedThreadPool() }

    internal fun setMainExecutor() {
        PMKConfiguration.Q = PMKConfiguration.Value(main, main)
    }
}
