package com.my.calculator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.calculator.R
import com.my.calculator.generated.CALC_VIEWPORT_HEIGHT
import com.my.calculator.generated.CALC_VIEWPORT_WIDTH
import com.my.calculator.generated.KEY_AREAS
import com.my.calculator.generated.KEY_BACKGROUND_DRAWABLES

private val DarkenColorFilter = ColorFilter.colorMatrix(
    ColorMatrix().apply { setToScale(0.85f, 0.85f, 0.85f, 1f) }
)

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(CALC_VIEWPORT_WIDTH / CALC_VIEWPORT_HEIGHT)
                .fillMaxSize()
        ) {
            // 1. Calculator Body
            Image(
                painter = painterResource(R.drawable.calc_body),
                contentDescription = "Calculator Body",
                modifier = Modifier.fillMaxSize()
            )

            // 2. Button Backgrounds (50 keys) with 150ms press darkening
            val pressed = viewModel.pressedKey
            for (area in KEY_AREAS) {
                val resId = KEY_BACKGROUND_DRAWABLES[area.code] ?: continue
                Image(
                    painter = painterResource(resId),
                    contentDescription = null,
                    colorFilter = if (pressed == area.code) DarkenColorFilter else null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. Comma / Point Label
            val commaRes = if (viewModel.settings.decimalFormat == "comma") {
                R.drawable.label_comma_de
            } else {
                R.drawable.label_comma_en
            }
            Image(
                painter = painterResource(commaRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            // 4. LCD Display Layer
            DisplayLayer(
                displayInput = viewModel.displayInput,
                displayOutput = viewModel.displayOutput,
                showCursor = true,
                modifier = Modifier.fillMaxSize()
            )

            // 5. Indicators
            IndicatorLayer(
                modes = viewModel.modes,
                calcMode = viewModel.settings.calcMode,
                angleMode = viewModel.settings.angleMode,
                roundingMode = viewModel.settings.roundingMode,
                modifier = Modifier.fillMaxSize()
            )

            // 6. Keypad Touch Handler
            KeypadLayer(
                onKeyDown = { key -> viewModel.onKeyPressed(key) },
                modifier = Modifier.fillMaxSize()
            )

            // 7. Version Label (bottom left)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 8.dp)
                    .clickable { viewModel.showWhatsNew = true }
            ) {
                Text(
                    text = "v1.0",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            // 8. Settings Button (bottom right)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 8.dp)
                    .background(Color(0xFF1E1E2F), RoundedCornerShape(4.dp))
                    .clickable { viewModel.showSettings = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⚙️",
                    fontSize = 12.sp
                )
            }
        }

        // Settings Dialog
        if (viewModel.showSettings) {
            SettingsSheet(
                settings = viewModel.settings,
                onUpdatePreferDecimal = { viewModel.updatePreferDecimal(it) },
                onUpdateTurnOffClose = { viewModel.updateTurnOffClose(it) },
                onUpdateDecimalFormat = { viewModel.updateDecimalFormat(it) },
                onOpenLicenses = {
                    viewModel.showSettings = false
                    viewModel.showLicenses = true
                },
                onDismiss = { viewModel.showSettings = false }
            )
        }

        // Licenses Dialog
        if (viewModel.showLicenses) {
            LicensesSheet(onDismiss = { viewModel.showLicenses = false })
        }

        // What's New Dialog
        if (viewModel.showWhatsNew) {
            WhatsNewSheet(onDismiss = { viewModel.showWhatsNew = false })
        }

        // Alert Dialog
        if (viewModel.showAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.showAlert = false },
                title = { Text("Notice") },
                text = { Text(viewModel.alertMessage) },
                confirmButton = {
                    Button(onClick = { viewModel.showAlert = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
