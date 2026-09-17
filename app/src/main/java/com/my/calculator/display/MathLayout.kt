package com.my.calculator.display

import android.graphics.Paint
import android.graphics.Typeface
import kotlin.math.max

data class LayoutBox(
    val node: DisplayNode,
    var width: Float = 0f,
    var height: Float = 0f,
    var ascent: Float = 0f,
    var descent: Float = 0f,
    var x: Float = 0f,
    var y: Float = 0f,
    val children: MutableList<LayoutBox> = mutableListOf(),
    var isCursor: Boolean = false
)

class MathMeasurer(
    private val regularTypeface: Typeface,
    private val italicTypeface: Typeface,
    val rootFontSizePx: Float
) {
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = regularTypeface
        textSize = rootFontSizePx
        color = 0xFF3F4266.toInt()
    }

    fun measure(node: DisplayNode, fontSize: Float = rootFontSizePx): LayoutBox {
        val paint = Paint(basePaint).apply {
            textSize = fontSize
        }
        val fm = paint.fontMetrics
        val baseAscent = -fm.ascent
        val baseDescent = fm.descent
        val baseHeight = baseAscent + baseDescent
        val rem = rootFontSizePx

        return when (node) {
            is DisplayNode.Text -> {
                paint.typeface = if (node.italic) italicTypeface else regularTypeface
                val w = paint.measureText(node.text)
                LayoutBox(node, width = w, height = baseHeight, ascent = baseAscent, descent = baseDescent)
            }
            is DisplayNode.Placeholder -> {
                val w = paint.measureText("▯")
                LayoutBox(node, width = w, height = baseHeight, ascent = baseAscent, descent = baseDescent)
            }
            is DisplayNode.Cursor -> {
                LayoutBox(node, width = 4f, height = baseHeight, ascent = baseAscent, descent = baseDescent, isCursor = true)
            }
            is DisplayNode.LineBreak -> {
                LayoutBox(node, width = 0f, height = baseHeight, ascent = baseAscent, descent = baseDescent)
            }
            is DisplayNode.Frac -> {
                val fracFontSize = 0.667f * rem
                val topBox = measure(node.top, fracFontSize)
                val bottomBox = measure(node.bottom, fracFontSize)
                val padLeft = 0.111f * rem
                val marginR = 0.111f * rem
                val barW = max(topBox.width, bottomBox.width) + 2 * padLeft
                val totalW = barW + marginR

                topBox.x = (barW - topBox.width) / 2f
                bottomBox.x = (barW - bottomBox.width) / 2f

                val barThickness = 0.111f * rem
                val gap = 0.111f * rem

                topBox.y = 0f
                bottomBox.y = topBox.height + barThickness + 2 * gap

                val totalH = bottomBox.y + bottomBox.height
                val box = LayoutBox(node, width = totalW, height = totalH, ascent = totalH / 2f, descent = totalH / 2f)
                box.children.add(topBox)
                box.children.add(bottomBox)
                box
            }
            is DisplayNode.Sqrt -> {
                val radBox = measure(node.radicand, fontSize)
                val sqrtW = paint.measureText("√")
                val padR = 0.222f * fontSize
                val totalW = sqrtW + radBox.width + padR
                val totalH = max(baseHeight, radBox.height)

                radBox.x = sqrtW
                radBox.y = (totalH - radBox.height) / 2f

                val box = LayoutBox(node, width = totalW, height = totalH, ascent = baseAscent, descent = baseDescent)
                box.children.add(radBox)
                box
            }
            is DisplayNode.SupScript -> {
                val subFontSize = 0.667f * rem
                val contentBox = measure(node.content, subFontSize)
                val shiftY = -0.35f * fontSize
                contentBox.y = shiftY
                val box = LayoutBox(node, width = contentBox.width, height = contentBox.height, ascent = baseAscent + 0.35f * fontSize, descent = baseDescent)
                box.children.add(contentBox)
                box
            }
            is DisplayNode.SubScript -> {
                val subFontSize = 0.667f * rem
                val contentBox = measure(node.content, subFontSize)
                val shiftY = 0.35f * fontSize
                contentBox.y = shiftY
                val box = LayoutBox(node, width = contentBox.width, height = contentBox.height, ascent = baseAscent, descent = baseDescent + 0.35f * fontSize)
                box.children.add(contentBox)
                box
            }
            is DisplayNode.Pow -> {
                val baseBox = measure(node.base, fontSize)
                val expBox = measure(node.exp, 0.667f * rem)
                expBox.x = baseBox.width
                expBox.y = -0.35f * fontSize
                val totalW = baseBox.width + expBox.width
                val totalH = max(baseBox.height, expBox.height + 0.35f * fontSize)
                val box = LayoutBox(node, width = totalW, height = totalH, ascent = baseAscent, descent = baseDescent)
                box.children.add(baseBox)
                box.children.add(expBox)
                box
            }
            is DisplayNode.Row -> {
                var curX = 0f
                var maxAscent = baseAscent
                var maxDescent = baseDescent
                val childrenBoxes = mutableListOf<LayoutBox>()

                for (ch in node.children) {
                    val cb = measure(ch, fontSize)
                    cb.x = curX
                    curX += cb.width
                    maxAscent = max(maxAscent, cb.ascent)
                    maxDescent = max(maxDescent, cb.descent)
                    childrenBoxes.add(cb)
                }

                val totalH = maxAscent + maxDescent
                for (cb in childrenBoxes) {
                    cb.y = maxAscent - cb.ascent
                }

                val box = LayoutBox(node, width = curX, height = totalH, ascent = maxAscent, descent = maxDescent)
                box.children.addAll(childrenBoxes)
                box
            }
            else -> {
                LayoutBox(node, width = 0f, height = baseHeight, ascent = baseAscent, descent = baseDescent)
            }
        }
    }
}
