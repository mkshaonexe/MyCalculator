package com.my.calculator.core.math

object SexagesimalRewriter {
    private val rules = listOf(
        Regex("""([\d.e]*°){4}""") to "error",
        Regex("""([\d.e]*°){3}[\d.e]+""") to "error",
        Regex("""([\d.e]+)°([\d.e]+)°([\d.e]+)°""") to "[$1+$2/60+$3/3600][1]",
        Regex("""([\d.e]*°){3}""") to "error",
        Regex("""([\d.e]*°){2}[\d.e]+""") to "error",
        Regex("""([\d.e]+)°([\d.e]+)°""") to "[$1+$2/60][1]",
        Regex("""([\d.e]*°){2}""") to "error",
        Regex("""([\d.e]*°){1}[\d.e]+""") to "error",
        Regex("""([\d.e]+)°""") to "[$1][1]",
        Regex("""°""") to "error"
    )

    fun rewrite(input: String): String {
        var current = input
        for ((pattern, replacement) in rules) {
            current = pattern.replace(current, replacement)
        }
        return current
    }
}
