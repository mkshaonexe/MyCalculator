package com.my.calculator.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ConstantsTest {

    @Test
    fun testConstantsCountAndValues() {
        assertEquals(43, CONST_CHARS.size)
        assertEquals(43, CONST_VALUES.size)

        // Test key constants
        assertEquals(1.672621637e-27, CONST_VALUES[0], 1e-35) // mP
        assertEquals(299792458.0, CONST_VALUES[27], 1e-9)      // c0
        assertEquals(9.80665, CONST_VALUES[34], 1e-9)          // g
        assertEquals(3.141592653589793, CONST_VALUES[40], 1e-15) // pi
        assertEquals(2.718281828459045, CONST_VALUES[41], 1e-15) // e
        assertEquals(0.01, CONST_VALUES[42], 1e-9)             // %
    }
}
