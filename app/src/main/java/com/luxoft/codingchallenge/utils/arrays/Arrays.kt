package com.luxoft.codingchallenge.utils.arrays

import kotlin.math.abs

fun IntArray.findClosest(to: Int): Int {
    return reduce { best: Int, current: Int ->
        if (abs(current - to) <= abs(best - to)) current else best
    }
}