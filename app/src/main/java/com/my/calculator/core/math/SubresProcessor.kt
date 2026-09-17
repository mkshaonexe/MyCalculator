package com.my.calculator.core.math

object SubresProcessor {

    fun process(
        expr: String,
        evalFunction: (String, Map<String, MathValue>) -> MathValue
    ): String {
        var res = expr
        while (res.contains("subres")) {
            val subresIdx = res.indexOf("subres")
            val afterSubres = res.substring(subresIdx + "subres".length)
            val idEndIdx = afterSubres.indexOf("id")
            if (idEndIdx == -1) break
            val id = afterSubres.substring(0, idEndIdx)

            val startIndicator = "subres${id}idstart"
            val endIndicator = "subres${id}idend"
            val insertIndicator = "subres${id}idinsert"
            val paramIndicator = "subres${id}idparam"

            val startIdx = res.indexOf(startIndicator)
            val endIdx = res.indexOf(endIndicator)
            val insertIdx = res.indexOf(insertIndicator)

            if (startIdx == -1 || endIdx == -1 || insertIdx == -1) break

            val paramsStr = res.substring(startIdx + startIndicator.length, endIdx)
            val params = paramsStr.split(paramIndicator)

            val computedValue = if (params.size == 2) {
                derivate(params[0], params[1], evalFunction)
            } else if (params.size == 3) {
                integrate(params[0], params[1], params[2], evalFunction)
            } else {
                throw IllegalArgumentException("Invalid subres parameter count: ${params.size}")
            }

            val valStr = when (computedValue) {
                is MathValue.Real -> computedValue.value.toString()
                is MathValue.Cx -> if (computedValue.im == 0.0) computedValue.re.toString() else "(${computedValue.re}+${computedValue.im}i)"
            }

            res = res.substring(0, insertIdx) + valStr + res.substring(insertIdx + insertIndicator.length)

            val newStartIdx = res.indexOf(startIndicator)
            val newEndIdx = res.indexOf(endIndicator)
            res = res.substring(0, newStartIdx) + res.substring(newEndIdx + endIndicator.length)
        }
        return res
    }

    fun derivate(
        eq: String,
        xExpr: String,
        evalFunction: (String, Map<String, MathValue>) -> MathValue
    ): MathValue {
        val xVal = evalFunction(xExpr, emptyMap())
        val xDouble = when (xVal) {
            is MathValue.Real -> xVal.value
            is MathValue.Cx -> xVal.re
        }
        val h = 7e-4
        val yMin = evalFunction(eq, mapOf("X" to MathValue.Real(xDouble - h)))
        val yMax = evalFunction(eq, mapOf("X" to MathValue.Real(xDouble + h)))

        return if (yMin is MathValue.Real && yMax is MathValue.Real) {
            MathValue.Real((yMax.value - yMin.value) / (2.0 * h))
        } else {
            val ca = toCx(yMax)
            val cb = toCx(yMin)
            val diff = ComplexMath.sub(ca, cb)
            MathValue.Cx(diff.re / (2.0 * h), diff.im / (2.0 * h))
        }
    }

    fun integrate(
        eq: String,
        aExpr: String,
        bExpr: String,
        evalFunction: (String, Map<String, MathValue>) -> MathValue
    ): MathValue {
        val aVal = evalFunction(aExpr, emptyMap())
        val bVal = evalFunction(bExpr, emptyMap())
        val xMin = when (aVal) {
            is MathValue.Real -> aVal.value
            is MathValue.Cx -> aVal.re
        }
        val xMax = when (bVal) {
            is MathValue.Real -> bVal.value
            is MathValue.Cx -> bVal.re
        }

        val nSteps = 50_000
        val h = (xMax - xMin) / nSteps

        val tokens = Tokenizer(eq).tokenize()
        val ast = Parser(tokens).parse()

        var sumReal = 0.0
        var sumIm = 0.0
        var hasComplex = false

        var y0Real = 0.0
        var y0Im = 0.0
        var ynReal = 0.0
        var ynIm = 0.0

        for (i in 0..nSteps) {
            val xCurrent = xMin + i * h
            val evaluator = Evaluator(scope = mapOf("X" to MathValue.Real(xCurrent)))
            val y = evaluator.evaluate(ast)
            when (y) {
                is MathValue.Real -> {
                    sumReal += y.value
                    if (i == 0) y0Real = y.value
                    if (i == nSteps) ynReal = y.value
                }
                is MathValue.Cx -> {
                    hasComplex = true
                    sumReal += y.re
                    sumIm += y.im
                    if (i == 0) { y0Real = y.re; y0Im = y.im }
                    if (i == nSteps) { ynReal = y.re; ynIm = y.im }
                }
            }
        }

        val finalReal = (sumReal - 0.5 * y0Real - 0.5 * ynReal) * h
        val roundedReal = MathFunctions.roundSignificant(finalReal, 10)

        return if (hasComplex) {
            val finalIm = (sumIm - 0.5 * y0Im - 0.5 * ynIm) * h
            val roundedIm = MathFunctions.roundSignificant(finalIm, 10)
            MathValue.Cx(roundedReal, roundedIm)
        } else {
            MathValue.Real(roundedReal)
        }
    }

    private fun toCx(v: MathValue): MathValue.Cx = when (v) {
        is MathValue.Real -> MathValue.Cx(v.value, 0.0)
        is MathValue.Cx -> v
    }
}
