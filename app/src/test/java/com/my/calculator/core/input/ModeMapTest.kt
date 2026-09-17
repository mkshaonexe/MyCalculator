package com.my.calculator.core.input

import org.junit.Assert.assertEquals
import org.junit.Test

class ModeMapTest {

    @Test
    fun testShiftMapMappings() {
        assertEquals("key_pi", SHIFT_MAP["key_pow10"])
        assertEquals("key_STO", SHIFT_MAP["key_rcl"])
        assertEquals("key_sqrt3", SHIFT_MAP["key_sqrt"])
        assertEquals("key_pow3", SHIFT_MAP["key_pow2"])
        assertEquals("key_sqrtn", SHIFT_MAP["key_pown"])
        assertEquals("key_faculty", SHIFT_MAP["key_pow_minus1"])
        assertEquals("key_epow", SHIFT_MAP["key_ln"])
        assertEquals("key_sin_minus1", SHIFT_MAP["key_sin"])
        assertEquals("key_cos_minus1", SHIFT_MAP["key_cos"])
        assertEquals("key_tan_minus1", SHIFT_MAP["key_tan"])
        assertEquals("key_M_minus", SHIFT_MAP["key_M_plus"])
        assertEquals("key_CONST", SHIFT_MAP["key_7"])
        assertEquals("key_deriv", SHIFT_MAP["key_integ"])
        assertEquals("key_setup", SHIFT_MAP["key_mode"])
        assertEquals("key_back", SHIFT_MAP["key_eng"])
        assertEquals("key_off", SHIFT_MAP["key_ac"])
        assertEquals("key_perc", SHIFT_MAP["key_lparen"])
    }

    @Test
    fun testAlphaMapMappings() {
        assertEquals("key_e", ALPHA_MAP["key_pow10"])
        assertEquals("key_uservar_A", ALPHA_MAP["key_neg"])
        assertEquals("key_uservar_B", ALPHA_MAP["key_deg"])
        assertEquals("key_uservar_C", ALPHA_MAP["key_hyp"])
        assertEquals("key_uservar_D", ALPHA_MAP["key_sin"])
        assertEquals("key_uservar_E", ALPHA_MAP["key_cos"])
        assertEquals("key_uservar_F", ALPHA_MAP["key_tan"])
        assertEquals("key_uservar_X", ALPHA_MAP["key_rparen"])
        assertEquals("key_uservar_Y", ALPHA_MAP["key_SD"])
        assertEquals("key_uservar_M", ALPHA_MAP["key_M_plus"])
    }

    @Test
    fun testStoMapMappings() {
        assertEquals("key_STO_A", STO_MAP["key_neg"])
        assertEquals("key_STO_B", STO_MAP["key_deg"])
        assertEquals("key_STO_C", STO_MAP["key_hyp"])
        assertEquals("key_STO_D", STO_MAP["key_sin"])
        assertEquals("key_STO_E", STO_MAP["key_cos"])
        assertEquals("key_STO_F", STO_MAP["key_tan"])
        assertEquals("key_STO_X", STO_MAP["key_rparen"])
        assertEquals("key_STO_Y", STO_MAP["key_SD"])
        assertEquals("key_STO_M", STO_MAP["key_M_plus"])
    }

    @Test
    fun testCmplxMapMappings() {
        assertEquals("key_i", CMPLX_MAP["key_eng"])
    }
}
