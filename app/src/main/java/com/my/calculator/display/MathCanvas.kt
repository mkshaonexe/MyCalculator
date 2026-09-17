package com.my.calculator.display

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

@Composable
fun MathCanvas(
    layoutBox: LayoutBox,
    regularTypeface: Typeface,
    italicTypeface: Typeface,
    rootFontSizePx: Float,
    scrollX: Float = 0f,
    scrollY: Float = 0f,
    showCursor: Boolean = true,
    alignRight: Boolean = false,
    availableWidth: Float = 0f,
    availableHeight: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3F4266.toInt()
        textSize = rootFontSizePx
        typeface = regularTypeface
    }

    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3F4266.toInt()
        strokeWidth = 0.111f * rootFontSizePx
        style = Paint.Style.STROKE
    }

    val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3F4266.toInt()
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawIntoCanvas { composeCanvas ->
            val nativeCanvas = composeCanvas.nativeCanvas
            nativeCanvas.save()

            val padLeft = if (!alignRight) 0.111f * rootFontSizePx else 0f
            val padTop = if (!alignRight) 0.222f * rootFontSizePx else 0f

            val startX = if (alignRight && availableWidth > layoutBox.width) {
                availableWidth - layoutBox.width
            } else {
                padLeft - scrollX
            }

            val startY = if (alignRight && availableHeight > layoutBox.height) {
                availableHeight - layoutBox.height
            } else {
                padTop - scrollY
            }

            nativeCanvas.translate(startX, startY)

            drawLayout(
                canvas = nativeCanvas,
                box = layoutBox,
                curX = 0f,
                curY = 0f,
                textPaint = textPaint,
                linePaint = linePaint,
                cursorPaint = cursorPaint,
                regularTypeface = regularTypeface,
                italicTypeface = italicTypeface,
                rootFontSizePx = rootFontSizePx,
                cursorVisible = showCursor && cursorAlpha > 0.5f
            )

            nativeCanvas.restore()
        }
    }
}

private fun drawLayout(
    canvas: Canvas,
    box: LayoutBox,
    curX: Float,
    curY: Float,
    textPaint: Paint,
    linePaint: Paint,
    cursorPaint: Paint,
    regularTypeface: Typeface,
    italicTypeface: Typeface,
    rootFontSizePx: Float,
    cursorVisible: Boolean
) {
    val x = curX + box.x
    val y = curY + box.y

    when (val node = box.node) {
        is DisplayNode.Text -> {
            textPaint.typeface = if (node.italic) italicTypeface else regularTypeface
            textPaint.textSize = if (box.fontSize > 0f) box.fontSize else rootFontSizePx
            canvas.drawText(node.text, x, y + box.ascent, textPaint)
        }
        is DisplayNode.Placeholder -> {
            textPaint.typeface = regularTypeface
            textPaint.textSize = if (box.fontSize > 0f) box.fontSize else rootFontSizePx
            canvas.drawText("▯", x, y + box.ascent, textPaint)
        }
        is DisplayNode.Cursor -> {
            if (cursorVisible) {
                canvas.drawLine(x, y, x, y + box.height, cursorPaint)
            }
        }
        is DisplayNode.Frac -> {
            // Draw children
            for (ch in box.children) {
                drawLayout(
                    canvas, ch, x, y,
                    textPaint, linePaint, cursorPaint,
                    regularTypeface, italicTypeface, rootFontSizePx, cursorVisible
                )
            }
            // Draw fraction bar
            if (box.children.size >= 2) {
                val topChild = box.children[0]
                val barY = y + topChild.height + 0.111f * rootFontSizePx
                val barW = box.width - 0.111f * rootFontSizePx
                canvas.drawLine(x, barY, x + barW, barY, linePaint)
            }
        }
        is DisplayNode.Sqrt -> {
            // Draw radicand
            for (ch in box.children) {
                drawLayout(
                    canvas, ch, x, y,
                    textPaint, linePaint, cursorPaint,
                    regularTypeface, italicTypeface, rootFontSizePx, cursorVisible
                )
            }
            // Draw checkmark and overbar
            textPaint.typeface = regularTypeface
            val sqrtFontSize = if (box.fontSize > 0f) box.fontSize else rootFontSizePx
            textPaint.textSize = sqrtFontSize
            canvas.drawText("√", x, y + box.ascent, textPaint)
            val sqrtW = textPaint.measureText("√")
            val barY = y + 0.111f * sqrtFontSize
            canvas.drawLine(x + sqrtW, barY, x + box.width, barY, linePaint)
        }
        else -> {
            for (ch in box.children) {
                drawLayout(
                    canvas, ch, x, y,
                    textPaint, linePaint, cursorPaint,
                    regularTypeface, italicTypeface, rootFontSizePx, cursorVisible
                )
            }
        }
    }
}
