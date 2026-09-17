package com.my.calculator.core.input

class ComplexEquationHandler(
    parentHandler: EquationListHandler,
    context: CalculatorContext
) : EquationInputHandler(parentHandler, context) {
    override fun handle(inputCode: String) {
        val mapped = CMPLX_MAP[inputCode] ?: inputCode
        super.handle(mapped)
    }
}
