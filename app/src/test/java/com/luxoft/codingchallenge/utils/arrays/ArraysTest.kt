package com.luxoft.codingchallenge.utils.arrays

import org.junit.Test

import org.junit.Assert.*

class ArraysTest {

    @Test
    fun findClosest() {
        assertEquals(3 ,intArrayOf(1,3,4,10).findClosest(2))
        assertEquals(1 ,intArrayOf(1,3,4,10).findClosest(-10))
        assertEquals(1 ,intArrayOf(1,3,4,10).findClosest(1))
        assertEquals(10 ,intArrayOf(1,3,4,10).findClosest(15))
        assertEquals(10 ,intArrayOf(1,3,4,10).findClosest(10))
        assertEquals(4 ,intArrayOf(1,3,4,10).findClosest(4))
        assertEquals(4 ,intArrayOf(1,3,4,10).findClosest(5))
        assertEquals(10 ,intArrayOf(1,3,4,10).findClosest(9))
    }
}