package com.my.calculator.core.format

import kotlin.math.abs
import kotlin.math.floor

object ContinuedFraction {

    fun parseContinuedFraction(continuedFraction: List<Long>): Pair<Long, Long> {
        var res0 = 1L
        var res1 = 0L
        for (level in continuedFraction.indices.reversed()) {
            val newRes0 = continuedFraction[level] * res0 + res1
            res1 = res0
            res0 = newRes0
        }
        return Pair(res0, res1)
    }

    fun decimalToContinuedFraction(decimal: Double, epsilon: Double = 1e-16): Pair<List<Long>, Pair<Long, Long>> {
        var newDecimal = decimal
        var thisInt = floor(newDecimal).toLong()
        val res = mutableListOf(thisInt)
        var remainder = newDecimal - thisInt
        var resultingFraction = parseContinuedFraction(res)

        var guard = 0
        while (abs(resultingFraction.first.toDouble() / resultingFraction.second.toDouble() - decimal) > epsilon) {
            if (++guard > 10_000) break
            if (abs(remainder) < 1e-16) break
            newDecimal = 1.0 / remainder
            thisInt = floor(newDecimal).toLong()
            res.add(thisInt)
            remainder = newDecimal - thisInt
            resultingFraction = parseContinuedFraction(res)
        }

        return Pair(res, resultingFraction)
    }
}
