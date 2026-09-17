package com.my.calculator.core.input

data class CalculatorContext(
    var calcMode: String = "COMP",
    var angleMode: String = "Deg",
    var roundingMode: String = "Norm_1",
    var turnOffClose: Boolean = false,
    var preferDecimal: Boolean = false,
    var userLang: String = "en-US",
    val modes: MutableMap<String, Boolean> = mutableMapOf(
        "shift" to false,
        "alpha" to false,
        "STO" to false
    ),
    var activeHandler: InputHandler? = null,
    var onReloadApp: (() -> Unit)? = null,
    var onCloseApp: (() -> Unit)? = null,
    var onSetCalcMode: ((String) -> Unit)? = null,
    var onSetSetupSetting: ((String) -> Unit)? = null
) {
    fun toggleMode(target: String?) {
        for (key in listOf("shift", "alpha", "STO")) {
            modes[key] = (key == target) && !(modes[key] ?: false)
        }
    }
}

abstract class InputHandler {
    var displayInput: String = ""
    var displayOutput: String = ""
    var isAppClosed: Boolean = false

    abstract fun handle(inputCode: String)
}
