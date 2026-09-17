package com.my.calculator.ui

import android.graphics.RectF
import android.graphics.Region
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.graphics.PathParser
import com.my.calculator.generated.CALC_VIEWPORT_HEIGHT
import com.my.calculator.generated.CALC_VIEWPORT_WIDTH
import com.my.calculator.generated.KEY_AREAS

class KeyHitTester {
    private val regions: List<Pair<String, Region>> = KEY_AREAS.map { area ->
        val androidPath = PathParser.createPathFromPathData(area.pathData)
        val r = RectF()
        androidPath.computeBounds(r, true)
        val region = Region().apply {
            setPath(
                androidPath,
                Region(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
            )
        }
        area.code to region
    }

    fun hitTest(vx: Float, vy: Float): String? {
        val x = vx.toInt()
        val y = vy.toInt()
        return regions.asReversed().firstOrNull { it.second.contains(x, y) }?.first
    }
}

@Composable
fun KeypadLayer(
    onKeyDown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hitTester = remember { KeyHitTester() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val size = this.size
                    if (size.width > 0 && size.height > 0) {
                        val vx = (down.position.x / size.width.toFloat()) * CALC_VIEWPORT_WIDTH
                        val vy = (down.position.y / size.height.toFloat()) * CALC_VIEWPORT_HEIGHT
                        val key = hitTester.hitTest(vx, vy)
                        if (key != null) {
                            onKeyDown(key)
                            down.consume()
                        }
                    }
                }
            }
    )
}
