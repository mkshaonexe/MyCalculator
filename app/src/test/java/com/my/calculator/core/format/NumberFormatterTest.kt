package com.my.calculator.core.format

import com.my.calculator.core.math.MathValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberFormatterTest {

    @Test
    fun testFractionFormatting() {
        val half = NumberFormatter.formatNumber(MathValue.Real(0.5), formatAs = "fraction")
        assertEquals("<span class=\"frac_wrapper\"><span class=\"frac_top\">1</span><span class=\"frac_bottom\">2</span></span>", half)

        val frac19_6 = NumberFormatter.formatNumber(MathValue.Real(19.0 / 6.0), formatAs = "fraction")
        assertEquals("<span class=\"frac_wrapper\"><span class=\"frac_top\">19</span><span class=\"frac_bottom\">6</span></span>", frac19_6)

        // Integer should format as decimal (denominator = 1)
        val four = NumberFormatter.formatNumber(MathValue.Real(4.0), formatAs = "fraction")
        assertEquals("4", four)
    }

    @Test
    fun testDecimalFormatting() {
        val pi = NumberFormatter.formatNumber(MathValue.Real(3.141592653589793), formatAs = "decimal")
        assertEquals("3.141592654", pi)

        val zero = NumberFormatter.formatNumber(MathValue.Real(0.0), formatAs = "decimal")
        assertEquals("0", zero)

        val deFormatted = NumberFormatter.formatNumber(MathValue.Real(2.5), formatAs = "decimal", userLang = "de-DE")
        assertEquals("2,5", deFormatted)
    }

    @Test
    fun testSexagesimalFormatting() {
        val result = NumberFormatter.formatNumber(MathValue.Real(1.123456), formatAs = "sexagesimal")
        assertEquals("1°7′24.44‴", result)
    }

    @Test
    fun testEngFormatting() {
        val engResult = NumberFormatter.formatNumber(MathValue.Real(1.5), formatAs = "eng5")
        assertEquals("0.0015<span class=\"pow10\">×⒑</span><span class=\"pow_top\">3</span>", engResult)
    }

    @Test
    fun testComplexFormatting() {
        val c = MathValue.Cx(3.0, 0.25)
        val formatted = NumberFormatter.formatNumber(c, formatAs = "decimal")
        assertEquals("3<br>+0.25i", formatted)

        val cFrac = MathValue.Cx(-11.5, 5.625)
        val fracFormatted = NumberFormatter.formatNumber(cFrac, formatAs = "fraction")
        assertTrue(fracFormatted.contains("<span class=\"frac_wrapper\">"))
        assertTrue(fracFormatted.contains("i"))
    }
}
