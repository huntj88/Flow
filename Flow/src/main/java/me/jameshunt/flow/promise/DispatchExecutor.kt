package me.jameshunt.flow.promise

import android.os.Handler
import android.os.Looper
import com.inmotionsoftware.promisekt.PMKConfiguration
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.map
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.max

object DispatchExecutor {
    val main: Executor by lazy { Executor { Handler(Looper.getMainLooper()).post(it) } }
    val global: Executor by lazy { Executors.newCachedThreadPool() }

    init {
        PMKConfiguration.Q = PMKConfiguration.Value(main, main)
    }
}
