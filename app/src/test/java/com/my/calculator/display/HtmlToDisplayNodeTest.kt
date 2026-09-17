package com.my.calculator.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlToDisplayNodeTest {

    @Test
    fun testPlainTextParse() {
        val node = HtmlToDisplayNode.parse("123+456")
        assertTrue(node is DisplayNode.Text)
        assertEquals("123+456", (node as DisplayNode.Text).text)
    }

    @Test
    fun testFractionParse() {
        val html = "<span class='frac_wrapper'><span class='frac_top'>1</span><span class='frac_bottom'>2</span></span>"
        val node = HtmlToDisplayNode.parse(html)
        assertTrue(node is DisplayNode.Frac)
        val frac = node as DisplayNode.Frac
        assertEquals("1", (frac.top as DisplayNode.Text).text)
        assertEquals("2", (frac.bottom as DisplayNode.Text).text)
    }

    @Test
    fun testSqrtParse() {
        val html = "<span class='sqrt_wrapper'><span class='scale_height'>√</span><span class='sqrt'>9</span></span>"
        val node = HtmlToDisplayNode.parse(html)
        assertTrue(node is DisplayNode.Sqrt)
        val sqrt = node as DisplayNode.Sqrt
        assertEquals("9", (sqrt.radicand as DisplayNode.Text).text)
    }

    @Test
    fun testCursorAndPlaceholderParse() {
        val html = "5+<span class='cursor'>\uE000</span>▯"
        val node = HtmlToDisplayNode.parse(html)
        assertTrue(node is DisplayNode.Row)
        val row = node as DisplayNode.Row
        assertEquals(3, row.children.size)
        assertTrue(row.children[0] is DisplayNode.Text)
        assertTrue(row.children[1] is DisplayNode.Cursor)
        assertTrue(row.children[2] is DisplayNode.Placeholder)
    }
}
