package com.my.calculator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.my.calculator.ui.CalculatorScreen
import com.my.calculator.ui.CalculatorViewModel

@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel = viewModel()
) {
    // Back button handling: dismiss overlays first
    BackHandler(
        enabled = viewModel.showSettings || viewModel.showLicenses || viewModel.showWhatsNew || viewModel.showAlert
    ) {
        when {
            viewModel.showLicenses -> viewModel.showLicenses = false
            viewModel.showSettings -> viewModel.showSettings = false
            viewModel.showWhatsNew -> viewModel.showWhatsNew = false
            viewModel.showAlert -> viewModel.showAlert = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CalculatorScreen(viewModel = viewModel)
    }
}
