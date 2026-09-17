package com.my.calculator.core.input

import com.my.calculator.core.math.CalcMode
import com.my.calculator.core.math.EvalResult
import com.my.calculator.core.math.MathEngine
import com.my.calculator.core.math.MathValue
import com.my.calculator.core.model.TreeBuilder

open class EquationInputHandler(
    val parentHandler: EquationListHandler,
    val context: CalculatorContext
) : InputHandler() {

    var inputCodeHistory: MutableList<String> = mutableListOf()

    override fun handle(inputCode: String) {
        if (inputCode == "key_shift") {
            context.toggleMode("shift")
            updateDisplay(true)
            return
        }
        if (inputCode == "key_alpha") {
            context.toggleMode("alpha")
            updateDisplay(true)
            return
        }

        var mappedCode: String? = inputCode
        when {
            context.modes["shift"] == true -> {
                context.toggleMode("none")
                mappedCode = SHIFT_MAP[inputCode]
                if (mappedCode == "key_STO") {
                    context.toggleMode("STO")
                    updateDisplay(true)
                    return
                }
            }
            context.modes["alpha"] == true -> {
                context.toggleMode("none")
                mappedCode = ALPHA_MAP[inputCode]
            }
            context.modes["STO"] == true -> {
                context.toggleMode("none")
                mappedCode = STO_MAP[inputCode]
            }
        }

        if (mappedCode != null) {
            inputCodeHistory.add(mappedCode)
        } else {
            updateDisplay(true)
            return
        }

        val lastCode = inputCodeHistory.lastOrNull()
        if (lastCode in listOf(
            "key_STO_A", "key_STO_B", "key_STO_C", "key_STO_D", "key_STO_E", "key_STO_F",
            "key_STO_X", "key_STO_Y", "key_STO_M", "key_M_plus", "key_M_minus"
        )) {
            inputCodeHistory.add(inputCodeHistory.size - 1, "end")
            inputCodeHistory.add("key_equals")
        }

        val finalLastCode = inputCodeHistory.lastOrNull()
        when (finalLastCode) {
            "key_ac" -> {
                inputCodeHistory.clear()
                context.toggleMode("none")
            }
            "key_CONST" -> {
                inputCodeHistory.removeLastOrNull()
                val h = ConstSelectHandler(this, context)
                context.activeHandler = h
                h.updateDisplay()
                return
            }
            "key_hyp" -> {
                inputCodeHistory.removeLastOrNull()
                val h = HypSelectHandler(this, context)
                context.activeHandler = h
                h.updateDisplay()
                return
            }
            "key_mode" -> {
                inputCodeHistory.removeLastOrNull()
                val h = ModeSelectHandler(this, context)
                context.activeHandler = h
                h.updateDisplay()
                return
            }
            "key_setup" -> {
                inputCodeHistory.removeLastOrNull()
                val h = SetupSelectHandler(this, context)
                context.activeHandler = h
                h.updateDisplay()
                return
            }
            "key_off" -> {
                if (context.turnOffClose) {
                    context.onCloseApp?.invoke()
                } else {
                    val h = TurnedOffHandler(context)
                    context.activeHandler = h
                    h.updateDisplay()
                }
                return
            }
        }

        updateDisplay(true)
    }

    fun updateDisplay(showCursor: Boolean = true) {
        val treeRes = TreeBuilder.build(
            history = inputCodeHistory,
            angleMode = context.angleMode,
            userLang = context.userLang,
            userVars = parentHandler.userVars,
            lastResult = parentHandler.results.lastOrNull(),
            showCursor = showCursor
        )

        if (treeRes.calcOutput) {
            val noCursorRes = TreeBuilder.build(
                history = inputCodeHistory,
                angleMode = context.angleMode,
                userLang = context.userLang,
                userVars = parentHandler.userVars,
                lastResult = parentHandler.results.lastOrNull(),
                showCursor = false
            )

            val expr = noCursorRes.exprString
            val xVal = parentHandler.userVars["X"] ?: MathValue.Real(0.0)
            val mode = if (context.calcMode == "CMPLX") CalcMode.CMPLX else CalcMode.COMP

            val eval = MathEngine.evaluate(
                exprString = expr,
                scope = mapOf("X" to xVal),
                calcMode = mode
            )

            when (eval) {
                is EvalResult.Error -> {
                    inputCodeHistory.removeLastOrNull()
                    displayInput = "error"
                    displayOutput = ""
                }
                is EvalResult.Success -> {
                    val resVal = eval.value
                    val sto = treeRes.stoNode
                    if (sto != null) {
                        val varName = sto.varName ?: "M"
                        if (varName == "M") {
                            val currentM = (parentHandler.userVars["M"] as? MathValue.Real)?.value ?: 0.0
                            val addVal = (resVal as? MathValue.Real)?.value ?: 0.0
                            parentHandler.userVars["M"] = MathValue.Real(currentM + sto.sign * addVal)
                        } else {
                            parentHandler.userVars[varName] = resVal
                        }
                    }

                    context.activeHandler = parentHandler
                    while (parentHandler.inputStrings.size <= parentHandler.displayEquationIndex) {
                        parentHandler.inputStrings.add("")
                        parentHandler.results.add(MathValue.Real(0.0))
                    }
                    parentHandler.inputStrings[parentHandler.displayEquationIndex] = noCursorRes.displayString
                    parentHandler.results[parentHandler.displayEquationIndex] = resVal
                    parentHandler.updateDisplay(false)
                }
            }
        } else {
            displayInput = treeRes.displayString
            displayOutput = ""
        }
    }
}
