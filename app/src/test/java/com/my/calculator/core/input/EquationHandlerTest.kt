package com.my.calculator.core.input

import com.my.calculator.core.math.MathValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EquationHandlerTest {

    @Test
    fun testBasicEquationEvaluation() {
        val context = CalculatorContext()
        val listHandler = EquationListHandler(context)
        val active = context.activeHandler as EquationInputHandler

        // Type: 1 + 2 =
        active.handle("key_1")
        active.handle("key_plus")
        active.handle("key_2")
        active.handle("key_equals")

        // Active handler is now listHandler
        assertEquals(listHandler, context.activeHandler)
        assertEquals(1, listHandler.results.size)
        assertEquals(3.0, (listHandler.results[0] as MathValue.Real).value, 1e-9)
        assertEquals("3", listHandler.displayOutput)
    }

    @Test
    fun testReplayAndAnsChaining() {
        val context = CalculatorContext()
        val listHandler = EquationListHandler(context)
        var active = context.activeHandler as EquationInputHandler

        // 5 * 6 = 30
        active.handle("key_5")
        active.handle("key_x")
        active.handle("key_6")
        active.handle("key_equals")

        // Now in listHandler, press + 4 -> should auto-prepend Ans
        listHandler.handle("key_plus")
        active = context.activeHandler as EquationInputHandler
        active.handle("key_4")
        active.handle("key_equals")

        assertEquals(2, listHandler.results.size)
        assertEquals(34.0, (listHandler.results[1] as MathValue.Real).value, 1e-9)
    }

    @Test
    fun testVariableStoreAndRecall() {
        val context = CalculatorContext()
        val listHandler = EquationListHandler(context)
        val active = context.activeHandler as EquationInputHandler

        // Type 42 -> shift -> rcl (STO) -> neg (A)
        active.handle("key_4")
        active.handle("key_2")
        active.handle("key_shift")
        active.handle("key_rcl") // activates STO
        active.handle("key_neg") // STO_A

        // Variable A should be 42
        val varA = listHandler.userVars["A"]
        assertNotNull(varA)
        assertEquals(42.0, (varA as MathValue.Real).value, 1e-9)
    }

    @Test
    fun testFormatToggle() {
        val context = CalculatorContext()
        val listHandler = EquationListHandler(context)
        val active = context.activeHandler as EquationInputHandler

        // 1 / 2 =
        active.handle("key_1")
        active.handle("key_div")
        active.handle("key_2")
        active.handle("key_equals")

        // Default fraction
        assertTrue(listHandler.displayOutput.contains("<span class=\"frac_wrapper\">"))

        // Toggle SD -> decimal
        listHandler.handle("key_SD")
        assertEquals("0.5", listHandler.displayOutput)

        // Toggle SD again -> fraction
        listHandler.handle("key_SD")
        assertTrue(listHandler.displayOutput.contains("<span class=\"frac_wrapper\">"))
    }
}
