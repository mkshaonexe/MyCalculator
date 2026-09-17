package com.my.calculator.core.math

import kotlin.math.*

object ComplexMath {
    fun abs(c: MathValue.Cx): Double = hypot(c.re, c.im)
    fun arg(c: MathValue.Cx): Double = atan2(c.im, c.re)

    fun add(a: MathValue.Cx, b: MathValue.Cx): MathValue.Cx =
        MathValue.Cx(a.re + b.re, a.im + b.im)

    fun sub(a: MathValue.Cx, b: MathValue.Cx): MathValue.Cx =
        MathValue.Cx(a.re - b.re, a.im - b.im)

    fun mul(a: MathValue.Cx, b: MathValue.Cx): MathValue.Cx =
        MathValue.Cx(a.re * b.re - a.im * b.im, a.re * b.im + a.im * b.re)

    fun div(a: MathValue.Cx, b: MathValue.Cx): MathValue.Cx {
        val denom = b.re * b.re + b.im * b.im
        return MathValue.Cx(
            (a.re * b.re + a.im * b.im) / denom,
            (a.im * b.re - a.re * b.im) / denom
        )
    }

    fun exp(c: MathValue.Cx): MathValue.Cx {
        val r = kotlin.math.exp(c.re)
        return MathValue.Cx(r * cos(c.im), r * sin(c.im))
    }

    fun log(c: MathValue.Cx): MathValue.Cx =
        MathValue.Cx(kotlin.math.log(abs(c), E), arg(c))

    fun pow(base: MathValue.Cx, exponent: MathValue.Cx): MathValue.Cx {
        if (base.re == 0.0 && base.im == 0.0) {
            return if (exponent.re == 0.0 && exponent.im == 0.0) MathValue.Cx(1.0, 0.0) else MathValue.Cx(0.0, 0.0)
        }
        return exp(mul(exponent, log(base)))
    }

    fun sqrt(c: MathValue.Cx): MathValue.Cx = pow(c, MathValue.Cx(0.5, 0.0))

    fun nthRoots(c: MathValue.Cx, n: Int): List<MathValue.Cx> {
        if (n <= 0) return emptyList()
        val r = abs(c)
        val theta = arg(c)
        val rootR = r.pow(1.0 / n)
        val roots = mutableListOf<MathValue.Cx>()
        for (k in 0 until n) {
            val angle = (theta + 2.0 * Math.PI * k) / n
            var re = rootR * cos(angle)
            var im = rootR * sin(angle)
            if (kotlin.math.abs(re) < 1e-12 * rootR) re = 0.0
            if (kotlin.math.abs(im) < 1e-12 * rootR) im = 0.0
            roots.add(MathValue.Cx(re, im))
        }
        return roots
    }

    fun compareRoots(a: MathValue.Cx, b: MathValue.Cx): Int {
        return when {
            a.im == b.im -> a.re.compareTo(b.re)
            a.im == 0.0 -> 1
            b.im == 0.0 -> -1
            a.re == b.re -> a.im.compareTo(b.im)
            else -> a.re.compareTo(b.re)
        }
    }
}
