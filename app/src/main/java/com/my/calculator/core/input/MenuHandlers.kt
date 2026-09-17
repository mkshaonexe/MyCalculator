package com.my.calculator.core.input

class VoidHandler : InputHandler() {
    init {
        displayInput = "__app_closed__"
        displayOutput = "__app_closed__"
        isAppClosed = true
    }

    override fun handle(inputCode: String) {}
}

class TurnedOffHandler(private val context: CalculatorContext) : InputHandler() {
    init {
        updateDisplay()
    }

    fun updateDisplay() {
        displayInput = "OFF"
        displayOutput = ""
    }

    override fun handle(inputCode: String) {
        if (inputCode == "key_on") {
            context.onReloadApp?.invoke()
        }
        updateDisplay()
    }
}

class ConstSelectHandler(
    val parentHandler: InputHandler,
    val context: CalculatorContext
) : InputHandler() {
    val numbers: MutableList<String> = mutableListOf()
    private val maxInput = listOf("4", "0")

    init {
        updateDisplay()
    }

    fun updateDisplay() {
        displayInput = "KONSTANTE<br>Nummer 01~40?"
        var out = "["
        for (i in 0 until 2) {
            out += if (numbers.size > i) numbers[i] else "_"
        }
        out += "]"
        displayOutput = out
    }

    override fun handle(inputCode: String) {
        when {
            inputCode.startsWith("key_") && inputCode.length == 5 && inputCode[4].isDigit() -> {
                val num = inputCode.substring(4)
                numbers.add(num)
                var inputAllowed = true
                for (i in numbers.indices) {
                    if (numbers[i] < maxInput[i]) {
                        break
                    } else if (numbers[i] > maxInput[i]) {
                        inputAllowed = false
                        break
                    }
                }
                if (inputAllowed && numbers.size >= 2 && numbers.all { it == "0" }) {
                    inputAllowed = false
                }
                if (!inputAllowed) {
                    numbers.removeLastOrNull()
                }
            }
            inputCode == "key_ac" -> {
                context.activeHandler = parentHandler
                return
            }
            inputCode == "key_on" -> {
                context.onReloadApp?.invoke()
                return
            }
        }

        if (numbers.size >= 2) {
            context.activeHandler = parentHandler
            val resultingKey = "key_CONST_${numbers[0]}${numbers[1]}"
            if (parentHandler is EquationInputHandler) {
                parentHandler.inputCodeHistory.add(resultingKey)
                parentHandler.updateDisplay(true)
            }
            return
        }

        updateDisplay()
    }
}

class HypSelectHandler(
    val parentHandler: InputHandler,
    val context: CalculatorContext
) : InputHandler() {
    val numbers: MutableList<String> = mutableListOf()
    private val hypKeys = listOf(
        "key_sinh", "key_cosh", "key_tanh", "key_asinh", "key_acosh", "key_atanh"
    )

    init {
        updateDisplay()
    }

    fun updateDisplay() {
        val out = "1:sinh&nbsp;&nbsp;&nbsp;2:cosh<br>" +
                "3:tanh&nbsp;&nbsp;&nbsp;4:sinh-1<br>" +
                "5:cosh-1&nbsp;6:tanh-1"
        displayInput = "<span class='frac_top'>$out</span>"
        displayOutput = ""
    }

    override fun handle(inputCode: String) {
        when {
            inputCode.startsWith("key_") && inputCode.length == 5 && inputCode[4].isDigit() -> {
                val num = inputCode.substring(4)
                if (num in "1".."6") {
                    numbers.add(num)
                }
            }
            inputCode == "key_ac" -> {
                context.activeHandler = parentHandler
                return
            }
            inputCode == "key_on" -> {
                context.onReloadApp?.invoke()
                return
            }
        }

        if (numbers.isNotEmpty()) {
            context.activeHandler = parentHandler
            val idx = numbers[0].toInt() - 1
            if (idx in hypKeys.indices) {
                val resultingKey = hypKeys[idx]
                if (parentHandler is EquationInputHandler) {
                    parentHandler.inputCodeHistory.add(resultingKey)
                    parentHandler.updateDisplay(true)
                }
            }
            return
        }

        updateDisplay()
    }
}

class ModeSelectHandler(
    val parentHandler: InputHandler,
    val context: CalculatorContext
) : InputHandler() {
    private val calcModeMap = mapOf(
        "key_1" to "COMP",
        "key_2" to "CMPLX"
    )

    init {
        updateDisplay()
    }

    fun updateDisplay() {
        displayInput = "<span class='frac_top'>1:COMP&nbsp;&nbsp;&nbsp;2:CMPLX<br></span>"
        displayOutput = ""
    }

    override fun handle(inputCode: String) {
        if (inputCode in calcModeMap) {
            context.onSetCalcMode?.invoke(calcModeMap[inputCode]!!)
        } else if (inputCode == "key_ac" || inputCode == "key_mode") {
            context.activeHandler = parentHandler
        } else if (inputCode == "key_on") {
            context.onReloadApp?.invoke()
        }
        updateDisplay()
    }
}

class SetupSelectHandler(
    val parentHandler: InputHandler,
    val context: CalculatorContext
) : InputHandler() {
    private val setupMap = mapOf(
        "key_3" to "Deg",
        "key_4" to "Rad",
        "key_5" to "Gra",
        "key_6" to "Fix",
        "key_8" to "Norm"
    )
    private val subMenus = mapOf(
        "Fix" to "Fix 0~9?",
        "Norm" to "Norm 1~2?"
    )
    private val subSetupMap = mapOf(
        "Fix" to mapOf(
            "key_0" to "Fix_0", "key_1" to "Fix_1", "key_2" to "Fix_2", "key_3" to "Fix_3",
            "key_4" to "Fix_4", "key_5" to "Fix_5", "key_6" to "Fix_6", "key_7" to "Fix_7",
            "key_8" to "Fix_8", "key_9" to "Fix_9"
        ),
        "Norm" to mapOf(
            "key_1" to "Norm_1", "key_2" to "Norm_2"
        )
    )
    val inputHistory: MutableList<String> = mutableListOf()

    init {
        updateDisplay()
    }

    fun updateDisplay() {
        displayInput = if (inputHistory.isNotEmpty()) {
            val parentKey = setupMap[inputHistory[0]] ?: ""
            "<span class='frac_top'>${subMenus[parentKey] ?: ""}</span>"
        } else {
            val out = "1:______2:______<br>" +
                    "3:Deg&nbsp;&nbsp;&nbsp;4:Rad<br>" +
                    "5:Gra&nbsp;&nbsp;&nbsp;6:Fix<br>" +
                    "7:______8:Norm"
            "<span class='frac_top'>$out</span>"
        }
        displayOutput = ""
    }

    override fun handle(inputCode: String) {
        if (inputCode == "key_on") {
            context.onReloadApp?.invoke()
            return
        }
        if (inputCode == "key_ac" || inputCode == "key_mode") {
            context.activeHandler = parentHandler
            return
        }

        if (inputHistory.isEmpty()) {
            if (inputCode in setupMap) {
                inputHistory.add(inputCode)
                val setting = setupMap[inputCode]!!
                if (setting !in subMenus) {
                    context.onSetSetupSetting?.invoke(setting)
                }
            }
        } else {
            val subMenu = setupMap[inputHistory[0]]
            if (subMenu != null && subSetupMap[subMenu]?.containsKey(inputCode) == true) {
                inputHistory.add(inputCode)
                val setting = subSetupMap[subMenu]!![inputCode]!!
                context.onSetSetupSetting?.invoke(setting)
            }
        }

        updateDisplay()
    }
}
