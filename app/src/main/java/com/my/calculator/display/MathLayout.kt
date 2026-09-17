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
    var isCursor: Boolean = false,
    var fontSize: Float = 0f
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
                LayoutBox(node, width = w, height = baseHeight, ascent = baseAscent, descent = baseDescent, fontSize = fontSize)
            }
            is DisplayNode.Placeholder -> {
                val w = paint.measureText("▯")
                LayoutBox(node, width = w, height = baseHeight, ascent = baseAscent, descent = baseDescent, fontSize = fontSize)
            }
            is DisplayNode.Cursor -> {
                LayoutBox(node, width = 4f, height = baseHeight, ascent = baseAscent, descent = baseDescent, isCursor = true, fontSize = fontSize)
            }
            is DisplayNode.LineBreak -> {
                LayoutBox(node, width = 0f, height = baseHeight, ascent = baseAscent, descent = baseDescent, fontSize = fontSize)
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
                val box = LayoutBox(node, width = totalW, height = totalH, ascent = totalH / 2f, descent = totalH / 2f, fontSize = fontSize)
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

                val box = LayoutBox(node, width = totalW, height = totalH, ascent = baseAscent, descent = baseDescent, fontSize = fontSize)
                box.children.add(radBox)
                box
            }
            is DisplayNode.SupScript -> {
                val subFontSize = 0.667f * rem
                val contentBox = measure(node.content, subFontSize)
                val shiftUp = 0.45f * fontSize
                val ascent = contentBox.ascent + shiftUp
                val descent = max(0f, contentBox.descent - shiftUp)
                val totalH = ascent + descent
                contentBox.x = 0f
                contentBox.y = 0f
                val box = LayoutBox(node, width = contentBox.width, height = totalH, ascent = ascent, descent = descent, fontSize = fontSize)
                box.children.add(contentBox)
                box
            }
            is DisplayNode.SubScript -> {
                val subFontSize = 0.667f * rem
                val contentBox = measure(node.content, subFontSize)
                val shiftDown = 0.25f * fontSize
                val ascent = max(0f, contentBox.ascent - shiftDown)
                val descent = contentBox.descent + shiftDown
                val totalH = ascent + descent
                contentBox.x = 0f
                contentBox.y = if (contentBox.ascent < shiftDown) shiftDown - contentBox.ascent else 0f
                val box = LayoutBox(node, width = contentBox.width, height = totalH, ascent = ascent, descent = descent, fontSize = fontSize)
                box.children.add(contentBox)
                box
            }
            is DisplayNode.Pow -> {
                val baseBox = measure(node.base, fontSize)
                val subFontSize = 0.667f * rem
                val expBox = measure(node.exp, subFontSize)
                val shiftUp = 0.45f * fontSize
                val maxAscent = max(baseBox.ascent, expBox.ascent + shiftUp)
                val maxDescent = max(baseBox.descent, expBox.descent - shiftUp)
                val totalH = maxAscent + maxDescent
                val totalW = baseBox.width + expBox.width

                baseBox.x = 0f
                baseBox.y = maxAscent - baseBox.ascent

                expBox.x = baseBox.width
                expBox.y = maxAscent - (expBox.ascent + shiftUp)

                val box = LayoutBox(node, width = totalW, height = totalH, ascent = maxAscent, descent = maxDescent, fontSize = fontSize)
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

                val box = LayoutBox(node, width = curX, height = totalH, ascent = maxAscent, descent = maxDescent, fontSize = fontSize)
                box.children.addAll(childrenBoxes)
                box
            }
            else -> {
                LayoutBox(node, width = 0f, height = baseHeight, ascent = baseAscent, descent = baseDescent, fontSize = fontSize)
            }
        }
    }
}
