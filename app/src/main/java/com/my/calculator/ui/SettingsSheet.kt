package com.my.calculator.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.my.calculator.data.CalculatorSettings

private val BgDark = Color(0xFF1E1E2F)
private val ItemBg = Color(0xFF2C2C3E)
private val AccentColor = Color(0xFF696982)

@Composable
fun SettingsSheet(
    settings: CalculatorSettings,
    onUpdatePreferDecimal: (Boolean) -> Unit,
    onUpdateTurnOffClose: (Boolean) -> Unit,
    onUpdateDecimalFormat: (String) -> Unit,
    onOpenLicenses: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BgDark,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Decimal Format Dropdown / Choice
                Text(
                    text = "Decimal Format:",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isPoint = settings.decimalFormat != "comma"
                    Button(
                        onClick = { onUpdateDecimalFormat("point") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPoint) AccentColor else ItemBg
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("π = 3.14159…", fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = { onUpdateDecimalFormat("comma") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isPoint) AccentColor else ItemBg
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("π = 3,14159…", fontSize = 12.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prefer Decimal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdatePreferDecimal(!settings.preferDecimal) }
                ) {
                    Checkbox(
                        checked = settings.preferDecimal,
                        onCheckedChange = { onUpdatePreferDecimal(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentColor,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Prefer Decimal Numbers", color = Color.White, fontSize = 14.sp)
                }

                // Turn Off = Close App
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateTurnOffClose(!settings.turnOffClose) }
                ) {
                    Checkbox(
                        checked = settings.turnOffClose,
                        onCheckedChange = { onUpdateTurnOffClose(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentColor,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Turn Off = Close App", color = Color.White, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ItemBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // MK Shaon Profile
                Text(
                    text = "MK Shaon",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LinkItem(label = "GitHub", value = "https://github.com/mkshaonexe") {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mkshaonexe")))
                    } catch (_: ActivityNotFoundException) {}
                }

                LinkItem(label = "Website", value = "https://mkshaon.com") {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mkshaon.com")))
                    } catch (_: ActivityNotFoundException) {}
                }

                LinkItem(label = "Email", value = "mkshaondev@gmail.com") {
                    try {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mkshaondev@gmail.com")))
                    } catch (_: ActivityNotFoundException) {}
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ItemBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Open Source Licenses
                Button(
                    onClick = onOpenLicenses,
                    colors = ButtonDefaults.buttonColors(containerColor = ItemBg),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Source Licenses", color = Color.White, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkItem(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFFAAAAAA), fontSize = 13.sp)
        Text(text = value, color = Color(0xFF64B5F6), fontSize = 13.sp)
    }
}
