package com.my.calculator.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.my.calculator.core.input.CalculatorContext
import com.my.calculator.core.input.EquationListHandler
import com.my.calculator.core.input.InputHandler
import com.my.calculator.data.CalculatorSettings
import com.my.calculator.data.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    val settingsRepository = SettingsRepository(application)

    lateinit var calcContext: CalculatorContext
        private set

    lateinit var listHandler: EquationListHandler
        private set

    var activeHandler by mutableStateOf<InputHandler?>(null)
        private set

    var displayInput by mutableStateOf("")
        private set

    var displayOutput by mutableStateOf("")
        private set

    var modes by mutableStateOf<Map<String, Boolean>>(emptyMap())
        private set

    var pressedKey by mutableStateOf<String?>(null)
        private set

    var showSettings by mutableStateOf(false)
    var showLicenses by mutableStateOf(false)
    var showWhatsNew by mutableStateOf(false)
    var showAlert by mutableStateOf(false)
    var alertMessage by mutableStateOf("")

    var settings by mutableStateOf(CalculatorSettings())
        private set

    private var keyPressJob: Job? = null

    init {
        viewModelScope.launch {
            val initialSettings = settingsRepository.settingsFlow.first()
            settings = initialSettings
            initCalculator(initialSettings)

            settingsRepository.settingsFlow.collect { newSettings ->
                settings = newSettings
            }
        }
    }

    fun initCalculator(s: CalculatorSettings) {
        calcContext = CalculatorContext(
            calcMode = s.calcMode,
            angleMode = s.angleMode,
            roundingMode = s.roundingMode,
            turnOffClose = s.turnOffClose,
            preferDecimal = s.preferDecimal,
            userLang = if (s.decimalFormat == "comma") "de-DE" else "en-US",
            onReloadApp = { reloadApp() },
            onCloseApp = { handleCloseApp() },
            onSetCalcMode = { mode -> updateCalcMode(mode) },
            onSetSetupSetting = { setting -> updateSetupSetting(setting) }
        )

        listHandler = EquationListHandler(calcContext)
        calcContext.activeHandler = listHandler.equations[0]
        updateUiState()
    }

    fun onKeyPressed(code: String) {
        // Visual press brightness animation (150ms)
        pressedKey = code
        keyPressJob?.cancel()
        keyPressJob = viewModelScope.launch {
            delay(150)
            if (pressedKey == code) {
                pressedKey = null
            }
        }

        val handler = calcContext.activeHandler ?: listHandler
        handler.handle(code)
        updateUiState()
    }

    fun updateUiState() {
        val curHandler = calcContext.activeHandler ?: listHandler
        activeHandler = curHandler
        displayInput = curHandler.displayInput
        displayOutput = curHandler.displayOutput
        modes = calcContext.modes.toMap()
    }

    fun reloadApp() {
        initCalculator(settings)
    }

    private fun handleCloseApp() {
        // Handled at Activity level via finishAndRemoveTask or showAlert
        showAlert = true
        alertMessage = "App turned off."
    }

    fun updateCalcMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setCalcMode(mode)
            reloadApp()
        }
    }

    fun updateSetupSetting(setting: String) {
        viewModelScope.launch {
            when {
                setting in listOf("Deg", "Rad", "Gra") -> settingsRepository.setAngleMode(setting)
                setting.startsWith("Fix") || setting.startsWith("Norm") -> settingsRepository.setRoundingMode(setting)
            }
            reloadApp()
        }
    }

    fun updatePreferDecimal(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPreferDecimal(value)
            calcContext.preferDecimal = value
            listHandler.formatAs = if (value) "decimal" else "fraction"
            listHandler.updateDisplay()
            updateUiState()
        }
    }

    fun updateTurnOffClose(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTurnOffClose(value)
            calcContext.turnOffClose = value
        }
    }

    fun updateDecimalFormat(format: String) {
        viewModelScope.launch {
            settingsRepository.setDecimalFormat(format)
            calcContext.userLang = if (format == "comma") "de-DE" else "en-US"
            reloadApp()
        }
    }
}
