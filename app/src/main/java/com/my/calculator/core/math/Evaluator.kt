package com.my.calculator.core.math

import kotlin.math.*

class Evaluator(
    private val scope: Map<String, MathValue> = emptyMap(),
    private val placeholders: Map<String, MathValue> = emptyMap()
) {

    fun evaluate(node: AstNode): MathValue {
        return when (node) {
            is NumberNode -> MathValue.Real(node.value)
            is PlaceholderNode -> {
                placeholders[node.key]
                    ?: placeholders["@" + node.key]
                    ?: throw IllegalArgumentException("Unknown placeholder '@${node.key}'")
            }
            is IdentifierNode -> evaluateIdentifier(node.name)
            is UnaryOpNode -> evaluateUnary(node.op, node.expr)
            is BinaryOpNode -> evaluateBinary(node.op, node.left, node.right)
            is FunctionCallNode -> evaluateFunction(node.name, node.args)
            is MatrixIndexNode -> {
                val targetVal = evaluate(node.target)
                val indexVal = evaluate(node.index)
                // 1-based single element matrix indexing [expr][1]
                targetVal
            }
        }
    }

    private fun evaluateIdentifier(name: String): MathValue {
        return when (name) {
            "PI", "pi" -> MathValue.Real(Math.PI)
            "i" -> MathValue.Cx(0.0, 1.0)
            "e", "E" -> MathValue.Real(Math.E)
            "error", "undefined" -> throw IllegalArgumentException("Evaluation error: '$name'")
            else -> {
                scope[name]
                    ?: if (name == "X" || name == "x") MathValue.Real(0.0)
                    else throw IllegalArgumentException("Unknown identifier '$name'")
            }
        }
    }

    private fun evaluateUnary(op: UnaryOp, expr: AstNode): MathValue {
        val v = evaluate(expr)
        return when (op) {
            UnaryOp.NEGATE -> when (v) {
                is MathValue.Real -> MathValue.Real(-v.value)
                is MathValue.Cx -> MathValue.Cx(-v.re, -v.im)
            }
            UnaryOp.FACTORIAL -> when (v) {
                is MathValue.Real -> MathValue.Real(MathFunctions.factorial(v.value))
                is MathValue.Cx -> throw IllegalArgumentException("Factorial not supported for complex numbers")
            }
        }
    }

    private fun evaluateBinary(op: BinaryOp, left: AstNode, right: AstNode): MathValue {
        val a = evaluate(left)
        val b = evaluate(right)

        if (a is MathValue.Real && b is MathValue.Real) {
            return when (op) {
                BinaryOp.ADD -> MathValue.Real(a.value + b.value)
                BinaryOp.SUBTRACT -> MathValue.Real(a.value - b.value)
                BinaryOp.MULTIPLY -> MathValue.Real(a.value * b.value)
                BinaryOp.DIVIDE -> MathValue.Real(a.value / b.value)
                BinaryOp.POWER -> {
                    if (a.value >= 0.0 || b.value == floor(b.value)) {
                        MathValue.Real(a.value.pow(b.value))
                    } else {
                        ComplexMath.pow(toCx(a), toCx(b))
                    }
                }
            }
        }

        val ca = toCx(a)
        val cb = toCx(b)
        return when (op) {
            BinaryOp.ADD -> ComplexMath.add(ca, cb)
            BinaryOp.SUBTRACT -> ComplexMath.sub(ca, cb)
            BinaryOp.MULTIPLY -> ComplexMath.mul(ca, cb)
            BinaryOp.DIVIDE -> ComplexMath.div(ca, cb)
            BinaryOp.POWER -> ComplexMath.pow(ca, cb)
        }
    }

    private fun evaluateFunction(name: String, args: List<AstNode>): MathValue {
        val evaluatedArgs = args.map { evaluate(it) }

        return when (name) {
            "sin" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real) MathValue.Real(sin(arg.value))
                else {
                    val c = toCx(arg)
                    // sin(a + bi) = sin(a)cosh(b) + i cos(a)sinh(b)
                    MathValue.Cx(sin(c.re) * cosh(c.im), cos(c.re) * sinh(c.im))
                }
            }
            "cos" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real) MathValue.Real(cos(arg.value))
                else {
                    val c = toCx(arg)
                    // cos(a + bi) = cos(a)cosh(b) - i sin(a)sinh(b)
                    MathValue.Cx(cos(c.re) * cosh(c.im), -sin(c.re) * sinh(c.im))
                }
            }
            "tan" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real) MathValue.Real(tan(arg.value))
                else {
                    val sinV = evaluateFunction("sin", listOf(args[0]))
                    val cosV = evaluateFunction("cos", listOf(args[0]))
                    ComplexMath.div(toCx(sinV), toCx(cosV))
                }
            }
            "asin" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real && abs(arg.value) <= 1.0) {
                    MathValue.Real(asin(arg.value))
                } else {
                    // asin(z) = -i * log(i*z + sqrt(1 - z^2))
                    val z = toCx(arg)
                    val iz = MathValue.Cx(-z.im, z.re)
                    val oneMinusZ2 = ComplexMath.sub(MathValue.Cx(1.0, 0.0), ComplexMath.mul(z, z))
                    val sqrtPart = ComplexMath.sqrt(oneMinusZ2)
                    val inside = ComplexMath.add(iz, sqrtPart)
                    val logPart = ComplexMath.log(inside)
                    MathValue.Cx(logPart.im, -logPart.re)
                }
            }
            "acos" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real && abs(arg.value) <= 1.0) {
                    MathValue.Real(acos(arg.value))
                } else {
                    // acos(z) = pi/2 - asin(z)
                    val asinVal = evaluateFunction("asin", listOf(args[0]))
                    when (asinVal) {
                        is MathValue.Real -> MathValue.Real(Math.PI / 2.0 - asinVal.value)
                        is MathValue.Cx -> MathValue.Cx(Math.PI / 2.0 - asinVal.re, -asinVal.im)
                    }
                }
            }
            "atan" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real) {
                    MathValue.Real(atan(arg.value))
                } else {
                    // atan(z) = (i/2) * (log(1 - i*z) - log(1 + i*z))
                    val z = toCx(arg)
                    val iz = MathValue.Cx(-z.im, z.re)
                    val oneMinusIz = ComplexMath.sub(MathValue.Cx(1.0, 0.0), iz)
                    val onePlusIz = ComplexMath.add(MathValue.Cx(1.0, 0.0), iz)
                    val diff = ComplexMath.sub(ComplexMath.log(oneMinusIz), ComplexMath.log(onePlusIz))
                    // multiply by i/2: (re + i*im) * (i*0.5) = -im*0.5 + i*re*0.5
                    MathValue.Cx(-diff.im * 0.5, diff.re * 0.5)
                }
            }
            "sinh" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real) MathValue.Real(sinh(arg.value))
                else {
                    val c = toCx(arg)
                    MathValue.Cx(sinh(c.re) * cos(c.im), cosh(c.re) * sin(c.im))
                }
            }
            "cosh" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real) MathValue.Real(cosh(arg.value))
                else {
                    val c = toCx(arg)
                    MathValue.Cx(cosh(c.re) * cos(c.im), sinh(c.re) * sin(c.im))
                }
            }
            "tanh" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real) MathValue.Real(tanh(arg.value))
                else {
                    val sinhV = evaluateFunction("sinh", listOf(args[0]))
                    val coshV = evaluateFunction("cosh", listOf(args[0]))
                    ComplexMath.div(toCx(sinhV), toCx(coshV))
                }
            }
            "asinh" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real) {
                    MathValue.Real(ln(arg.value + sqrt(arg.value * arg.value + 1.0)))
                } else {
                    val z = toCx(arg)
                    val z2Plus1 = ComplexMath.add(ComplexMath.mul(z, z), MathValue.Cx(1.0, 0.0))
                    val sqrtPart = ComplexMath.sqrt(z2Plus1)
                    ComplexMath.log(ComplexMath.add(z, sqrtPart))
                }
            }
            "acosh" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real && arg.value >= 1.0) {
                    MathValue.Real(ln(arg.value + sqrt(arg.value * arg.value - 1.0)))
                } else {
                    val z = toCx(arg)
                    val zMinus1 = ComplexMath.sub(z, MathValue.Cx(1.0, 0.0))
                    val zPlus1 = ComplexMath.add(z, MathValue.Cx(1.0, 0.0))
                    val sqrtPart = ComplexMath.mul(ComplexMath.sqrt(zMinus1), ComplexMath.sqrt(zPlus1))
                    ComplexMath.log(ComplexMath.add(z, sqrtPart))
                }
            }
            "atanh" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real && abs(arg.value) < 1.0) {
                    MathValue.Real(0.5 * ln((1.0 + arg.value) / (1.0 - arg.value)))
                } else {
                    val z = toCx(arg)
                    val onePlusZ = ComplexMath.add(MathValue.Cx(1.0, 0.0), z)
                    val oneMinusZ = ComplexMath.sub(MathValue.Cx(1.0, 0.0), z)
                    val ratio = ComplexMath.div(onePlusZ, oneMinusZ)
                    val logPart = ComplexMath.log(ratio)
                    MathValue.Cx(logPart.re * 0.5, logPart.im * 0.5)
                }
            }
            "sind" -> MathValue.Real(MathFunctions.sind(toDouble(evaluatedArgs[0])))
            "cosd" -> MathValue.Real(MathFunctions.cosd(toDouble(evaluatedArgs[0])))
            "tand" -> MathValue.Real(MathFunctions.tand(toDouble(evaluatedArgs[0])))
            "sing" -> MathValue.Real(MathFunctions.sing(toDouble(evaluatedArgs[0])))
            "cosg" -> MathValue.Real(MathFunctions.cosg(toDouble(evaluatedArgs[0])))
            "tang" -> MathValue.Real(MathFunctions.tang(toDouble(evaluatedArgs[0])))
            "log" -> { // Natural logarithm (ln)
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real && arg.value > 0.0) {
                    MathValue.Real(ln(arg.value))
                } else {
                    ComplexMath.log(toCx(arg))
                }
            }
            "log10" -> {
                val arg = evaluatedArgs[0]
                if (arg is MathValue.Real && arg.value > 0.0) {
                    MathValue.Real(log10(arg.value))
                } else {
                    val lnVal = ComplexMath.log(toCx(arg))
                    val ln10 = ln(10.0)
                    MathValue.Cx(lnVal.re / ln10, lnVal.im / ln10)
                }
            }
            "nthRootComplex" -> {
                val n = toDouble(evaluatedArgs[0])
                MathFunctions.nthRootComplex(n, evaluatedArgs[1])
            }
            "round_significant", "roundSignificant" -> {
                val v = toDouble(evaluatedArgs[0])
                val places = toDouble(evaluatedArgs[1]).toInt()
                MathValue.Real(MathFunctions.roundSignificant(v, places))
            }
            else -> throw IllegalArgumentException("Unknown function '$name'")
        }
    }

    private fun toCx(v: MathValue): MathValue.Cx = when (v) {
        is MathValue.Real -> MathValue.Cx(v.value, 0.0)
        is MathValue.Cx -> v
    }

    private fun toDouble(v: MathValue): Double = when (v) {
        is MathValue.Real -> v.value
        is MathValue.Cx -> {
            if (v.im == 0.0) v.re
            else throw IllegalArgumentException("Expected real number but got complex $v")
        }
    }
}
