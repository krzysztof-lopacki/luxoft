package com.luxoft.codingchallenge.utils.rxjava

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

fun Completable.subscribeAndIgnoreErrors(subscriptionsManager: CompositeDisposable) {
    subscriptionsManager.add(onErrorComplete().subscribe())
}