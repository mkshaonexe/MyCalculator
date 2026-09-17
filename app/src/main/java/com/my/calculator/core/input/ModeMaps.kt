package com.my.calculator.core.input

val SHIFT_MAP: Map<String, String> = mapOf(
    "key_pow10"      to "key_pi",
    "key_rcl"        to "key_STO",
    "key_sqrt"       to "key_sqrt3",
    "key_pow2"       to "key_pow3",
    "key_pown"       to "key_sqrtn",
    "key_pow_minus1" to "key_faculty",
    "key_ln"         to "key_epow",
    "key_sin"        to "key_sin_minus1",
    "key_cos"        to "key_cos_minus1",
    "key_tan"        to "key_tan_minus1",
    "key_M_plus"     to "key_M_minus",
    "key_7"          to "key_CONST",
    "key_integ"      to "key_deriv",
    "key_mode"       to "key_setup",
    "key_eng"        to "key_back",
    "key_ac"         to "key_off",
    "key_lparen"     to "key_perc"
)

val ALPHA_MAP: Map<String, String> = mapOf(
    "key_pow10"  to "key_e",
    "key_neg"    to "key_uservar_A",
    "key_deg"    to "key_uservar_B",
    "key_hyp"    to "key_uservar_C",
    "key_sin"    to "key_uservar_D",
    "key_cos"    to "key_uservar_E",
    "key_tan"    to "key_uservar_F",
    "key_rparen" to "key_uservar_X",
    "key_SD"     to "key_uservar_Y",
    "key_M_plus" to "key_uservar_M"
)

val STO_MAP: Map<String, String> = mapOf(
    "key_neg"    to "key_STO_A",
    "key_deg"    to "key_STO_B",
    "key_hyp"    to "key_STO_C",
    "key_sin"    to "key_STO_D",
    "key_cos"    to "key_STO_E",
    "key_tan"    to "key_STO_F",
    "key_rparen" to "key_STO_X",
    "key_SD"     to "key_STO_Y",
    "key_M_plus" to "key_STO_M"
)

val CMPLX_MAP: Map<String, String> = mapOf(
    "key_eng" to "key_i"
)
