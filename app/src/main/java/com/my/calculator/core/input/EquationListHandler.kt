package com.my.calculator.core.input

import com.my.calculator.core.format.NumberFormatter
import com.my.calculator.core.math.MathValue
import kotlin.math.max
import kotlin.math.min

class EquationListHandler(
    val context: CalculatorContext
) : InputHandler() {

    val equations: MutableList<EquationInputHandler> = mutableListOf()
    val inputStrings: MutableList<String> = mutableListOf()
    val results: MutableList<MathValue> = mutableListOf()
    val userVars: MutableMap<String, MathValue> = mutableMapOf("M" to MathValue.Real(0.0))
    var displayEquationIndex: Int = 0
    var formatAs: String = if (context.preferDecimal) "decimal" else "fraction"

    init {
        addEmptyEquation()
        context.activeHandler = equations[0]
    }

    fun addEmptyEquation() {
        val eq = if (context.calcMode == "CMPLX") {
            ComplexEquationHandler(this, context)
        } else {
            EquationInputHandler(this, context)
        }
        equations.add(eq)
        formatAs = if (context.preferDecimal) "decimal" else "fraction"
    }

    fun selectEquation(up: Boolean) {
        val before = displayEquationIndex
        displayEquationIndex = if (up) {
            max(0, min(displayEquationIndex - 1, equations.size - 1))
        } else {
            max(0, min(displayEquationIndex + 1, equations.size - 1))
        }
        if (before != displayEquationIndex) {
            formatAs = if (context.preferDecimal) "decimal" else "fraction"
        }
    }

    override fun handle(inputCode: String) {
        var mappedCode = inputCode
        when {
            context.modes["shift"] == true -> {
                context.toggleMode("none")
                mappedCode = SHIFT_MAP[inputCode] ?: inputCode
            }
            context.modes["alpha"] == true -> {
                context.toggleMode("none")
                mappedCode = ALPHA_MAP[inputCode] ?: inputCode
            }
            context.modes["STO"] == true -> {
                context.toggleMode("none")
                mappedCode = STO_MAP[inputCode] ?: inputCode
            }
        }

        when (mappedCode) {
            "key_equals" -> {
                addEmptyEquation()
                val currentEq = equations[displayEquationIndex]
                val lastEq = equations.last()
                lastEq.inputCodeHistory = currentEq.inputCodeHistory.toMutableList()
                displayEquationIndex = equations.size - 1
                context.activeHandler = lastEq
                lastEq.updateDisplay(true)
            }
            "key_dir0" -> {
                addEmptyEquation()
                val currentEq = equations[displayEquationIndex]
                val lastEq = equations.last()
                lastEq.inputCodeHistory = if (currentEq.inputCodeHistory.isNotEmpty()) {
                    currentEq.inputCodeHistory.dropLast(1).toMutableList()
                } else {
                    mutableListOf()
                }
                displayEquationIndex = equations.size - 1
                context.activeHandler = lastEq
                lastEq.handle("end")
            }
            "key_dir2" -> {
                addEmptyEquation()
                val currentEq = equations[displayEquationIndex]
                val lastEq = equations.last()
                lastEq.inputCodeHistory = if (currentEq.inputCodeHistory.isNotEmpty()) {
                    currentEq.inputCodeHistory.dropLast(1).toMutableList()
                } else {
                    mutableListOf()
                }
                displayEquationIndex = equations.size - 1
                context.activeHandler = lastEq
                lastEq.handle("pos1")
            }
            "key_SD" -> {
                formatAs = if (formatAs == "fraction") "decimal" else "fraction"
                updateDisplay(false)
            }
            "key_deg" -> {
                formatAs = if (formatAs == "sexagesimal") "decimal" else "sexagesimal"
                updateDisplay(false)
            }
            "key_eng", "key_back" -> {
                if (formatAs.startsWith("eng")) {
                    val currentLevel = formatAs.substring(3).toIntOrNull() ?: 4
                    val dir = if (mappedCode == "key_back") -1 else 1
                    val newLevel = (currentLevel + dir).coerceIn(1, 9)
                    formatAs = "eng$newLevel"
                } else {
                    formatAs = "eng4"
                }
                updateDisplay(false)
            }
            "key_dir1" -> {
                selectEquation(false)
                updateDisplay(false)
            }
            "key_dir3" -> {
                selectEquation(true)
                updateDisplay(false)
            }
            "key_shift" -> {
                context.toggleMode("shift")
            }
            "key_alpha" -> {
                context.toggleMode("alpha")
            }
            "key_STO" -> {
                context.toggleMode("STO")
            }
            else -> {
                addEmptyEquation()
                displayEquationIndex = equations.size - 1
                val newEq = equations[displayEquationIndex]
                context.activeHandler = newEq

                if (mappedCode in listOf("key_plus", "key_minus", "key_div", "key_x") ||
                    mappedCode.startsWith("key_STO_")
                ) {
                    newEq.handle("key_Ans")
                }
                newEq.handle(mappedCode)
            }
        }
    }

    fun updateDisplay(showCursor: Boolean = false) {
        if (equations.isNotEmpty() && displayEquationIndex in inputStrings.indices) {
            displayInput = inputStrings[displayEquationIndex]
            val res = results.getOrNull(displayEquationIndex)
            displayOutput = if (res != null) {
                NumberFormatter.formatNumber(
                    value = res,
                    formatAs = formatAs,
                    userLang = context.userLang,
                    roundingMode = context.roundingMode
                )
            } else {
                ""
            }
        }
    }
}
