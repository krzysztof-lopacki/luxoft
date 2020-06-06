package com.luxoft.codingchallenge.utils.rxjava

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable

fun <T> Observable<T>.toLiveData(backpressureStrategy: BackpressureStrategy): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(toFlowable(backpressureStrategy))
}