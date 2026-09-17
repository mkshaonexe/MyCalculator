package com.my.calculator.core.format

import com.my.calculator.core.math.MathFunctions
import com.my.calculator.core.math.MathValue
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

object NumberFormatter {

    fun localeNumber(num: Double, userLang: String = "en-US", maxSignificantDigits: Int = 10): String {
        if (num == 0.0 || num == -0.0) return "0"
        if (num.isNaN() || num.isInfinite()) return num.toString()

        val sep = if (userLang.startsWith("de")) ',' else '.'

        return try {
            val bd = BigDecimal(num, MathContext(maxSignificantDigits, RoundingMode.HALF_UP)).stripTrailingZeros()
            val plain = bd.toPlainString()
            if (sep != '.') plain.replace('.', sep) else plain
        } catch (e: Exception) {
            num.toString()
        }
    }

    fun formatNumber(
        value: MathValue,
        formatAs: String = "fraction",
        userLang: String = "en-US",
        roundingMode: String = "Norm_1",
        epsilon: Double = 4.5e-16
    ): String {
        return when (value) {
            is MathValue.Cx -> {
                formatComplex(value.re, value.im, formatAs, userLang, roundingMode)
            }
            is MathValue.Real -> {
                formatDouble(value.value, formatAs, userLang, roundingMode, epsilon)
            }
        }
    }

    fun formatComplex(
        re: Double,
        im: Double,
        formatAs: String = "fraction",
        userLang: String = "en-US",
        roundingMode: String = "Norm_1"
    ): String {
        val reString = formatDouble(re, formatAs, userLang, roundingMode, epsilon = 1e-14)
        val imString = formatDouble(im, formatAs, userLang, roundingMode, epsilon = 1e-14)
        var resString = ""
        if (reString != "0") {
            resString += reString
        }
        if (imString != "0") {
            if (reString != "0") {
                if (formatAs != "fraction") {
                    resString += "<br>"
                }
                resString += "+"
            }
            if (imString != "1") {
                resString += imString
            }
            resString += "i"
        }
        return if (resString.isNotEmpty()) resString else "0"
    }

    fun formatDouble(
        inputNum: Double,
        formatAs: String = "fraction",
        userLang: String = "en-US",
        roundingMode: String = "Norm_1",
        epsilon: Double = 4.5e-16
    ): String {
        var num = inputNum
        if (num.isNaN()) return "NaN"
        if (num.isInfinite()) return if (num > 0) "Infinity" else "-Infinity"

        if (formatAs == "fraction") {
            val resultingFraction = ContinuedFraction.decimalToContinuedFraction(num, epsilon).second
            val len = resultingFraction.first.toString().length + resultingFraction.second.toString().length
            if (resultingFraction.second != 1L && len <= 9) {
                return "<span class=\"frac_wrapper\"><span class=\"frac_top\">${resultingFraction.first}</span><span class=\"frac_bottom\">${resultingFraction.second}</span></span>"
            }
        } else if (formatAs == "sexagesimal") {
            val sexaD = floor(num).toLong()
            val sexaM = floor((num - sexaD) * 60).toLong()
            val sexaS = round((num - sexaD - sexaM.toDouble() / 60.0) * 3600.0 * 100.0) / 100.0
            val sexaSStr = if (sexaS % 1.0 == 0.0) sexaS.toLong().toString() else sexaS.toString()
            return "${sexaD}°${sexaM}′${sexaSStr}‴"
        } else if (formatAs.startsWith("eng")) {
            val engLevel = formatAs.substring(3).toIntOrNull() ?: 4
            val thisExponent = (floor(log10(num) / 3.0).toInt() + engLevel - 4) * 3
            val thisFactor = 10.0.pow(thisExponent.toDouble())
            var resNumber = num / thisFactor

            var decimalPlaces = 9
            if (roundingMode.startsWith("Fix") && roundingMode.length >= 5 && roundingMode[4].isDigit()) {
                decimalPlaces = roundingMode[4].digitToInt()
            }
            resNumber = MathFunctions.round(resNumber, decimalPlaces)
            return localeNumber(resNumber, userLang, 10) + "<span class=\"pow10\">×⒑</span><span class=\"pow_top\">$thisExponent</span>"
        }

        if (roundingMode.startsWith("Fix") && roundingMode.length >= 5 && roundingMode[4].isDigit()) {
            val decimalPlaces = roundingMode[4].digitToInt()
            num = MathFunctions.round(num, decimalPlaces)
        }

        var lowerSciBorder = 1e-2
        if (roundingMode == "Norm_2") {
            lowerSciBorder = 1e-9
        }

        if (abs(num) >= 1e10 || (num != 0.0 && abs(num) < lowerSciBorder)) {
            val (coeff, exp) = toExponential9(num)
            val cleanExp = if (exp.startsWith("+")) exp.substring(1) else exp
            val coeffDouble = coeff.toDoubleOrNull() ?: 0.0
            return localeNumber(coeffDouble, userLang, 10) + "<span class=\"pow10\">×⒑</span><span class=\"pow_top\">$cleanExp</span>"
        }

        return localeNumber(num, userLang, 10)
    }

    private fun toExponential9(v: Double): Pair<String, String> {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("0.000000000E0", symbols)
        val s = df.format(v)
        val parts = s.split("E")
        return Pair(parts[0], parts.getOrElse(1) { "0" })
    }
}
