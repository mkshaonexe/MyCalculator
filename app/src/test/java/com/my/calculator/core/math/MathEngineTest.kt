package com.my.calculator.core.math

import org.junit.Assert.*
import org.junit.Test

class MathEngineTest {

    private fun eval(expr: String, calcMode: CalcMode = CalcMode.COMP): MathValue {
        val res = MathEngine.evaluate(expr, calcMode = calcMode)
        assertTrue("Expected success for '$expr'", res is EvalResult.Success)
        return (res as EvalResult.Success).value
    }

    private fun evalDouble(expr: String): Double {
        val v = eval(expr)
        assertTrue("Expected Real value for '$expr'", v is MathValue.Real)
        return (v as MathValue.Real).value
    }

    @Test
    fun testBasicArithmetic() {
        val res = evalDouble("12+4*5-8/3+7")
        assertEquals(36.333333333333336, res, 1e-9)
    }

    @Test
    fun testImplicitMultiplication() {
        val res = evalDouble("12(3.141592653589793)+5(3-7)-8log10(4)")
        assertEquals(12.88263191, res, 1e-8)
    }

    @Test
    fun testMatrixGroupingPower() {
        val res = evalDouble("([150238][1]^[2][1])")
        assertEquals(2.2571456644e10, res, 1.0)
    }

    @Test
    fun testNthRootComplexNegativeOdd() {
        val res = eval("nthRootComplex(3,(0-27))")
        assertTrue(res is MathValue.Real)
        assertEquals(-3.0, (res as MathValue.Real).value, 1e-9)
    }

    @Test
    fun testNthRootComplexNegativeEven() {
        val res = eval("nthRootComplex(2,(0-4))", calcMode = CalcMode.CMPLX)
        assertTrue(res is MathValue.Cx)
        val cx = res as MathValue.Cx
        assertEquals(0.0, cx.re, 1e-9)
        assertEquals(2.0, cx.im, 1e-9)
    }

    @Test
    fun testFactorial() {
        val res = evalDouble("8!")
        assertEquals(40320.0, res, 1e-9)
    }

    @Test
    fun testMatrixDivision() {
        val res = evalDouble("([1][1]/[2][1])")
        assertEquals(0.5, res, 1e-9)
    }

    @Test
    fun testLogBase() {
        val res = evalDouble("(1/log([3][1])*log([85][1]))")
        val expected = kotlin.math.ln(85.0) / kotlin.math.ln(3.0)
        assertEquals(expected, res, 1e-9)
    }

    @Test
    fun testScientificExponent() {
        val res = evalDouble("5e3")
        assertEquals(5000.0, res, 1e-9)
    }

    @Test
    fun testSexagesimalRewrite() {
        val res = evalDouble("1°30°")
        assertEquals(1.5, res, 1e-9)
    }

    @Test
    fun testTrigExactDegrees() {
        val s = evalDouble("sind(30)")
        assertEquals(0.5, s, 0.0)

        val c = evalDouble("cosd(90)")
        assertEquals(0.0, c, 0.0)
    }

    @Test
    fun testIntegration() {
        val res = SubresProcessor.integrate("X^2", "0", "1") { expr, scope ->
            MathEngine.evaluateRaw(expr, scope)
        }
        assertTrue(res is MathValue.Real)
        assertEquals(0.3333333333, (res as MathValue.Real).value, 1e-8)
    }

    @Test
    fun testDerivate() {
        val res = SubresProcessor.derivate("X^3", "2") { expr, scope ->
            MathEngine.evaluateRaw(expr, scope)
        }
        assertTrue(res is MathValue.Real)
        assertEquals(12.0, (res as MathValue.Real).value, 1e-3)
    }

    @Test
    fun testUnaryMinusPrecedence() {
        // -2^2 should be -(2^2) = -4
        val res = evalDouble("-2^2")
        assertEquals(-4.0, res, 1e-9)

        // (-2)^2 should be 4
        val res2 = evalDouble("(-2)^2")
        assertEquals(4.0, res2, 1e-9)
    }
}
