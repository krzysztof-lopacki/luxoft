package com.pioneer.drv.sdk.rxjava

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

fun createSingleThreadScheduler(name: String): Scheduler {
    val executor = Executors.newSingleThreadExecutor { operation ->
        return@newSingleThreadExecutor Thread(operation, name)
    }
    return Schedulers.from(executor)
}