package com.my.calculator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val BgDark = Color(0xFF1E1E2F)
private val ItemBg = Color(0xFF2C2C3E)
private val AccentColor = Color(0xFF696982)

@Composable
fun LicensesSheet(onDismiss: () -> Unit) {
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
                    text = "Open Source Licenses",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "My Calculator",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "© 2026 MK Shaon — GPL-3.0-only\nhttps://github.com/mkshaonexe",
                    color = Color(0xFFAAAAAA),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ItemBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Calculator artwork & display font",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "© Joris Yidong Scholl\nArtwork: CC BY-SA 4.0 · Font: GPL-3.0-only\nOriginal project: https://github.com/CardiJey/schulrechner",
                    color = Color(0xFFAAAAAA),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ItemBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "This application is free software licensed under the GNU General Public License v3.0. It comes with ABSOLUTELY NO WARRANTY.",
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

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
