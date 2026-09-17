package com.my.calculator.core.math

sealed interface MathValue {
    val isReal: Boolean get() = this is Real

    data class Real(val value: Double) : MathValue {
        override fun toString(): String = value.toString()
    }

    data class Cx(val re: Double, val im: Double) : MathValue {
        override fun toString(): String {
            return when {
                re == 0.0 && im == 1.0 -> "i"
                re == 0.0 && im == -1.0 -> "-i"
                re == 0.0 -> "${im}i"
                im == 1.0 -> "$re+i"
                im == -1.0 -> "$re-i"
                im < 0.0 -> "$re${im}i"
                else -> "$re+$im" + "i"
            }
        }
    }
}
