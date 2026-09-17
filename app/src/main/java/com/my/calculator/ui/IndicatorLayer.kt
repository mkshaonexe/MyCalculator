package com.my.calculator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.my.calculator.R

@Composable
fun IndicatorLayer(
    modes: Map<String, Boolean>,
    calcMode: String,
    angleMode: String,
    roundingMode: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (modes["shift"] == true) {
            Image(
                painter = painterResource(R.drawable.ind_shift),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (modes["alpha"] == true) {
            Image(
                painter = painterResource(R.drawable.ind_alpha),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (modes["STO"] == true) {
            Image(
                painter = painterResource(R.drawable.ind_sto),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (calcMode == "CMPLX") {
            Image(
                painter = painterResource(R.drawable.ind_cmplx),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (roundingMode.startsWith("Fix")) {
            Image(
                painter = painterResource(R.drawable.ind_fix),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        when (angleMode) {
            "Deg" -> Image(
                painter = painterResource(R.drawable.ind_deg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            "Rad" -> Image(
                painter = painterResource(R.drawable.ind_rad),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            "Gra" -> Image(
                painter = painterResource(R.drawable.ind_gra),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
