package com.my.calculator.core.math

enum class CalcMode {
    COMP, CMPLX
}

sealed interface EvalResult {
    data class Success(val value: MathValue) : EvalResult
    object Error : EvalResult
}

object MathEngine {

    fun evaluate(
        exprString: String,
        scope: Map<String, MathValue> = emptyMap(),
        placeholders: Map<String, MathValue> = emptyMap(),
        calcMode: CalcMode = CalcMode.COMP
    ): EvalResult {
        return try {
            val rewritten = SexagesimalRewriter.rewrite(exprString)
            if (rewritten.contains("error")) return EvalResult.Error

            val processed = SubresProcessor.process(rewritten) { subExpr, subScope ->
                val subResult = evaluateRaw(subExpr, scope + subScope, placeholders)
                subResult
            }

            val result = evaluateRaw(processed, scope, placeholders)

            if (isBadValue(result, calcMode)) {
                EvalResult.Error
            } else {
                EvalResult.Success(result)
            }
        } catch (e: Throwable) {
            EvalResult.Error
        }
    }

    fun evaluateRaw(
        expr: String,
        scope: Map<String, MathValue> = emptyMap(),
        placeholders: Map<String, MathValue> = emptyMap()
    ): MathValue {
        val tokens = Tokenizer(expr).tokenize()
        val ast = Parser(tokens).parse()
        return Evaluator(scope, placeholders).evaluate(ast)
    }

    private fun isBadValue(result: MathValue, calcMode: CalcMode): Boolean {
        return when (result) {
            is MathValue.Real -> !result.value.isFinite()
            is MathValue.Cx -> {
                if (calcMode != CalcMode.CMPLX) {
                    true // Complex result not allowed in COMP mode
                } else {
                    !result.re.isFinite() || !result.im.isFinite()
                }
            }
        }
    }
}
