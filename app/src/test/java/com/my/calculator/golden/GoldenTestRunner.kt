package com.my.calculator.golden

import com.my.calculator.core.input.CalculatorContext
import com.my.calculator.core.input.EquationListHandler
import com.my.calculator.core.input.VoidHandler
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class GoldenTestRunner {

    private fun normalize(str: String): String {
        return str
            .replace("\"", "'")
            .replace(Regex("\\s*style='[^']*'"), "")
            .replace(Regex("alignLeft\\d+"), "alignLeft")
            .replace(Regex("alignRight\\d+"), "alignRight")
    }

    private fun runGoldenTest(testCase: JSONObject) {
        val name = testCase.optString("name", "unnamed")
        val userLang = testCase.optString("userLang", "en-US")
        val preferDecimals = testCase.optBoolean("prefer_decimals", false)
        val calcMode = testCase.optString("calc_mode", "COMP")
        val roundingMode = testCase.optString("rounding_mode", "Norm_1")
        val angleMode = testCase.optString("angle_mode", "Deg")
        val turnOffClose = testCase.optBoolean("turn_off_close", false)

        val inputHistoryArray = testCase.getJSONArray("input_history")
        val inputHistory = mutableListOf<String>()
        for (i in 0 until inputHistoryArray.length()) {
            inputHistory.add(inputHistoryArray.getString(i))
        }

        val expectedInput = normalize(testCase.getString("rendered_input"))
        val expectedOutput = normalize(testCase.getString("rendered_output"))

        val context = CalculatorContext(
            calcMode = calcMode,
            angleMode = angleMode,
            roundingMode = roundingMode,
            turnOffClose = turnOffClose,
            preferDecimal = preferDecimals,
            userLang = userLang
        )

        val listHandler = EquationListHandler(context)
        context.activeHandler = listHandler.equations[0]

        context.onReloadApp = {
            val newListHandler = EquationListHandler(context)
            context.activeHandler = newListHandler.equations[0]
        }
        context.onCloseApp = {
            context.activeHandler = VoidHandler()
        }

        for (code in inputHistory) {
            val handler = context.activeHandler ?: listHandler
            handler.handle(code)
        }

        val active = context.activeHandler ?: listHandler
        val actualInput = normalize(active.displayInput)
        val actualOutput = normalize(active.displayOutput)

        assertEquals("[$name] Input mismatch", expectedInput, actualInput)
        assertEquals("[$name] Output mismatch", expectedOutput, actualOutput)
    }

    @Test
    fun testFraction2() {
        val jsonStream = javaClass.classLoader?.getResourceAsStream("golden/fraction_2.json")
            ?: error("golden/fraction_2.json not found")
        val content = jsonStream.bufferedReader().use { it.readText() }
        val testCase = JSONObject(content)
        runGoldenTest(testCase)
    }

    @Test
    fun testBatch1() {
        val jsonStream = javaClass.classLoader?.getResourceAsStream("golden/test_batch_1.json")
            ?: error("golden/test_batch_1.json not found")
        val content = jsonStream.bufferedReader().use { it.readText() }
        val batch = JSONArray(content)

        val failed = mutableListOf<String>()
        for (i in 0 until batch.length()) {
            val testCase = batch.getJSONObject(i)
            val name = testCase.optString("name", "test #$i")
            try {
                runGoldenTest(testCase)
            } catch (e: Throwable) {
                failed.add("[$i: $name] ${e.message}")
            }
        }

        if (failed.isNotEmpty()) {
            throw AssertionError("Failed ${failed.size} / ${batch.length()} golden tests:\n" + failed.take(15).joinToString("\n"))
        }
    }
}
