package com.my.calculator.core.model

import com.my.calculator.core.math.EvalResult
import com.my.calculator.core.math.MathEngine
import com.my.calculator.core.math.MathValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TreeBuilderTest {

    @Test
    fun testSimpleAddition() {
        val result = TreeBuilder.build(listOf("key_1", "key_plus", "key_2"))
        assertEquals("1+2", result.exprString)
        val eval = MathEngine.evaluate(result.exprString) as EvalResult.Success
        assertEquals(3.0, (eval.value as MathValue.Real).value, 1e-9)
    }

    @Test
    fun testEmptyFraction() {
        val result = TreeBuilder.build(listOf("key_frac"))
        // Cursor should be at numerator
        assertNotNull(result.startNode.right)
        assertTrue(result.exprString.contains("/["))
    }

    @Test
    fun testFractionWrapNumber() {
        // "5", "key_frac", "2"
        val result = TreeBuilder.build(listOf("key_5", "key_frac", "key_2"))
        assertEquals("([5][1]/[2][1])", result.exprString)
        val eval = MathEngine.evaluate(result.exprString) as EvalResult.Success
        assertEquals(2.5, (eval.value as MathValue.Real).value, 1e-9)
    }

    @Test
    fun testCursorNavigationAndDeletion() {
        // Type 1, 2, 3, del -> 12
        val res1 = TreeBuilder.build(listOf("key_1", "key_2", "key_3", "key_del"))
        assertEquals("12", res1.exprString)

        // Type 1, 2, left (key_dir0), 5 -> 152
        val res2 = TreeBuilder.build(listOf("key_1", "key_2", "key_dir0", "key_5"))
        assertEquals("152", res2.exprString)
    }

    @Test
    fun testSqrtContainer() {
        val result = TreeBuilder.build(listOf("key_sqrt", "key_9"))
        assertTrue(result.exprString.contains("nthRootComplex(2,[9][1])"))
        val eval = MathEngine.evaluate(result.exprString) as EvalResult.Success
        assertEquals(3.0, (eval.value as MathValue.Real).value, 1e-9)
    }

    @Test
    fun testTrigAngleModes() {
        val deg = TreeBuilder.build(listOf("key_sin", "key_9", "key_0", "key_rparen"), angleMode = "Deg")
        assertEquals("sind(90)", deg.exprString)
        val degEval = MathEngine.evaluate(deg.exprString) as EvalResult.Success
        assertEquals(1.0, (degEval.value as MathValue.Real).value, 1e-9)

        val rad = TreeBuilder.build(listOf("key_sin", "key_pi", "key_rparen"), angleMode = "Rad")
        assertEquals("sin((3.141592653589793))", rad.exprString)
        val radEval = MathEngine.evaluate(rad.exprString) as EvalResult.Success
        assertEquals(0.0, (radEval.value as MathValue.Real).value, 1e-9)
    }

    @Test
    fun testStoNode() {
        val result = TreeBuilder.build(listOf("key_5", "key_STO_A"))
        assertNotNull(result.stoNode)
        assertEquals("A", result.stoNode?.varName)
        assertEquals("5", result.exprString)
    }
}
