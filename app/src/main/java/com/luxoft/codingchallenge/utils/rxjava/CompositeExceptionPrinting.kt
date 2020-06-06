package com.luxoft.codingchallenge.utils.rxjava

import io.reactivex.exceptions.CompositeException
import java.lang.StringBuilder

fun CompositeException.allToString(): String {
    val message = StringBuilder()
    for (i in exceptions.indices) {
        message.append(exceptions[i].toString())
        if (i != exceptions.size - 1) message.append("\n")
    }
    return message.toString()
}