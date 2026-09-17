package com.my.calculator.core.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SexagesimalRewriterTest {

    @Test
    fun testSexagesimalRewrites() {
        // Rule: d°m°s° -> [d+m/60+s/3600][1]
        val r1 = SexagesimalRewriter.rewrite("12°30°45°")
        assertEquals("[12+30/60+45/3600][1]", r1)

        // Rule: d°m° -> [d+m/60][1]
        val r3 = SexagesimalRewriter.rewrite("1°20°")
        assertEquals("[1+20/60][1]", r3)

        // Rule: d° -> [d][1]
        val r5 = SexagesimalRewriter.rewrite("5°")
        assertEquals("[5][1]", r5)

        // Invalid consecutive degrees produce error
        val err = SexagesimalRewriter.rewrite("5°°")
        assertTrue(err.contains("error"))
    }
}
