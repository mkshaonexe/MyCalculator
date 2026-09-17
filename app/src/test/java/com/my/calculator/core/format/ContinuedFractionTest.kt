package com.my.calculator.core.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuedFractionTest {

    @Test
    fun testHalf() {
        val frac = ContinuedFraction.decimalToContinuedFraction(0.5).second
        assertEquals(1L, frac.first)
        assertEquals(2L, frac.second)
    }

    @Test
    fun testNineteenSixths() {
        val frac = ContinuedFraction.decimalToContinuedFraction(19.0 / 6.0).second
        assertEquals(19L, frac.first)
        assertEquals(6L, frac.second)
    }

    @Test
    fun testInteger() {
        val frac = ContinuedFraction.decimalToContinuedFraction(7.0).second
        assertEquals(7L, frac.first)
        assertEquals(1L, frac.second)
    }

    @Test
    fun testPiApproximation() {
        val frac = ContinuedFraction.decimalToContinuedFraction(Math.PI, epsilon = 1e-6).second
        // 355/113 is classic pi approximation
        assertEquals(355L, frac.first)
        assertEquals(113L, frac.second)
    }
}
