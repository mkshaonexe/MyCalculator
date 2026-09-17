package com.my.calculator.ui

import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.my.calculator.R
import com.my.calculator.display.HtmlToDisplayNode
import com.my.calculator.display.MathCanvas
import com.my.calculator.display.MathMeasurer
import com.my.calculator.generated.CALC_VIEWPORT_HEIGHT
import com.my.calculator.generated.CALC_VIEWPORT_WIDTH
import com.my.calculator.generated.DISPLAY_INPUT
import com.my.calculator.generated.DISPLAY_OUTPUT

@Composable
fun DisplayLayer(
    displayInput: String,
    displayOutput: String,
    showCursor: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val regularTypeface = remember {
        ResourcesCompat.getFont(context, R.font.schulrechner_regular) ?: Typeface.DEFAULT
    }
    val italicTypeface = remember {
        ResourcesCompat.getFont(context, R.font.schulrechner_italic) ?: Typeface.DEFAULT_BOLD
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        val scaleX = totalWidth.value / CALC_VIEWPORT_WIDTH
        val scaleY = totalHeight.value / CALC_VIEWPORT_HEIGHT

        val inX = DISPLAY_INPUT.left * scaleX
        val inY = DISPLAY_INPUT.top * scaleY
        val inW = DISPLAY_INPUT.width * scaleX
        val inH = DISPLAY_INPUT.height * scaleY

        val outX = DISPLAY_OUTPUT.left * scaleX
        val outY = DISPLAY_OUTPUT.top * scaleY
        val outW = DISPLAY_OUTPUT.width * scaleX
        val outH = DISPLAY_OUTPUT.height * scaleY

        val rootFontSizePx = with(density) { (outH.dp.toPx() * 0.29180534f) }

        val measurer = remember(rootFontSizePx, regularTypeface, italicTypeface) {
            MathMeasurer(regularTypeface, italicTypeface, rootFontSizePx)
        }

        // Input Box
        val inputNode = remember(displayInput) { HtmlToDisplayNode.parse(displayInput) }
        val inputBox = remember(inputNode, measurer) { measurer.measure(inputNode, rootFontSizePx) }

        // Output Box
        val outputNode = remember(displayOutput) { HtmlToDisplayNode.parse(displayOutput) }
        val outputBox = remember(outputNode, measurer) { measurer.measure(outputNode, rootFontSizePx) }

        val inWPx = with(density) { inW.dp.toPx() }
        val inHPx = with(density) { inH.dp.toPx() }
        val outWPx = with(density) { outW.dp.toPx() }
        val outHPx = with(density) { outH.dp.toPx() }

        // Input Canvas
        Box(
            modifier = Modifier
                .offset(x = inX.dp, y = inY.dp)
                .size(width = inW.dp, height = inH.dp)
                .clipToBounds()
        ) {
            val scrollX = if (inputBox.width > inWPx) inputBox.width - inWPx + 20f else 0f
            MathCanvas(
                layoutBox = inputBox,
                regularTypeface = regularTypeface,
                italicTypeface = italicTypeface,
                rootFontSizePx = rootFontSizePx,
                scrollX = scrollX,
                scrollY = 0f,
                showCursor = showCursor,
                alignRight = false,
                availableWidth = inWPx,
                availableHeight = inHPx
            )
        }

        // Output Canvas
        Box(
            modifier = Modifier
                .offset(x = outX.dp, y = outY.dp)
                .size(width = outW.dp, height = outH.dp)
                .clipToBounds()
        ) {
            MathCanvas(
                layoutBox = outputBox,
                regularTypeface = regularTypeface,
                italicTypeface = italicTypeface,
                rootFontSizePx = rootFontSizePx,
                scrollX = 0f,
                scrollY = 0f,
                showCursor = false,
                alignRight = true,
                availableWidth = outWPx,
                availableHeight = outHPx
            )
        }
    }
}
