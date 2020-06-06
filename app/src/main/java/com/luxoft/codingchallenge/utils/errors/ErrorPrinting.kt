package com.luxoft.codingchallenge.utils.errors

import com.luxoft.codingchallenge.utils.rxjava.allToString
import io.reactivex.exceptions.CompositeException

fun Throwable.resolveMessage(): String {
    return if (this is CompositeException) {
        this.allToString()
    } else {
        toString()
    }
}