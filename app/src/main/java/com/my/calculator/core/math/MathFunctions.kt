package com.my.calculator.core.math

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

object MathFunctions {

    fun round(v: Double, decimals: Int): Double {
        if (!v.isFinite()) return v
        return try {
            BigDecimal.valueOf(v).setScale(decimals, RoundingMode.HALF_UP).toDouble()
        } catch (e: Exception) {
            v
        }
    }

    fun roundSignificant(v: Double, places: Int): Double {
        if (v == 0.0 || !v.isFinite()) return v
        val absV = abs(v)
        val exp = ceil(log10(absV))
        val factor = 10.0.pow(exp)
        val divided = v / factor
        val rounded = round(divided, places)
        return rounded * factor
    }

    fun sind(x: Double): Double = round(sin(Math.PI / 180.0 * x), 15)
    fun cosd(x: Double): Double = round(cos(Math.PI / 180.0 * x), 15)
    fun tand(x: Double): Double = round(tan(Math.PI / 180.0 * x), 15)

    fun sing(x: Double): Double = round(sin(Math.PI / 200.0 * x), 15)
    fun cosg(x: Double): Double = round(cos(Math.PI / 200.0 * x), 15)
    fun tang(x: Double): Double = round(tan(Math.PI / 200.0 * x), 15)

    fun nthRootComplex(nVal: Double, input: MathValue): MathValue {
        val n = nVal.toInt()
        require(n > 0) { "Root degree must be positive" }

        val cx = when (input) {
            is MathValue.Real -> MathValue.Cx(input.value, 0.0)
            is MathValue.Cx -> input
        }

        // Fast path for real roots
        if (cx.im == 0.0) {
            val x = cx.re
            if (x >= 0.0) {
                return MathValue.Real(x.pow(1.0 / n))
            } else if (n % 2 != 0) {
                return MathValue.Real(-((-x).pow(1.0 / n)))
            }
        }

        val roots = ComplexMath.nthRoots(cx, n)
        val sorted = roots.sortedWith { a, b -> ComplexMath.compareRoots(a, b) }
        val best = sorted.last()
        return if (best.im == 0.0) {
            MathValue.Real(best.re)
        } else {
            best
        }
    }

    // Lanczos Gamma function approximation for non-integer / negative values
    fun gamma(z: Double): Double {
        if (z < 0.5) {
            // Reflection formula: Gamma(z) * Gamma(1-z) = pi / sin(pi * z)
            val sinPiZ = sin(Math.PI * z)
            if (sinPiZ == 0.0) return Double.NaN
            return Math.PI / (sinPiZ * gamma(1.0 - z))
        }
        val p = doubleArrayOf(
            676.5203681218851,
            -1259.1392167224028,
            771.32342877765313,
            -176.61502916214059,
            12.507343278686905,
            -0.138571095831109,
            9.9843695780195716e-6,
            1.5056327351493116e-7
        )
        val zm1 = z - 1.0
        var x = 0.99999999999980993
        for (i in p.indices) {
            x += p[i] / (zm1 + (i + 1))
        }
        val t = zm1 + p.size - 0.5
        return sqrt(2.0 * Math.PI) * t.pow(zm1 + 0.5) * exp(-t) * x
    }

    fun factorial(v: Double): Double {
        if (v < 0.0 && v == floor(v)) return Double.NaN
        if (v >= 0.0 && v == floor(v) && v <= 170.0) {
            val n = v.toInt()
            var res = 1.0
            for (i in 2..n) res *= i
            return res
        }
        return gamma(v + 1.0)
    }
}
