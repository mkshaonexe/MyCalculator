package com.my.calculator.core.model

import com.my.calculator.core.math.MathValue

open class MathNode(
    val kind: NodeKind,
    var display: String,
    var expr: String
) {
    var left: MathNode? = null
    var down: MathNode? = null
    var right: MathNode? = null
    var up: MathNode? = null

    var children: MutableList<MathNode>? = null
    var parent: MathNode? = null
    var isLastContainer: Boolean = false
    var skipToAfterCreation: MathNode? = null

    var varName: String? = null
    var constIndex: Int? = null
    var sign: Double = 1.0

    open fun getVarValue(
        userVars: Map<String, MathValue>,
        lastResult: MathValue?
    ): String {
        return when {
            varName == "X" -> "X"
            varName == "Ans" -> {
                when (lastResult) {
                    null -> "undefined"
                    is MathValue.Real -> lastResult.value.toString()
                    is MathValue.Cx -> if (lastResult.im == 0.0) lastResult.re.toString() else "(${lastResult.re}+${lastResult.im}i)"
                }
            }
            varName != null -> {
                val v = userVars[varName]
                when (v) {
                    null -> "0"
                    is MathValue.Real -> v.value.toString()
                    is MathValue.Cx -> if (v.im == 0.0) v.re.toString() else "(${v.re}+${v.im}i)"
                }
            }
            constIndex != null -> {
                CONST_VALUES[constIndex!!].toString()
            }
            else -> expr
        }
    }

    fun setNeighbor(dir: Int, neighbor: MathNode?) {
        when (dir) {
            0 -> left = neighbor
            1 -> down = neighbor
            2 -> right = neighbor
            3 -> up = neighbor
        }
    }

    fun getNeighbor(dir: Int): MathNode? {
        return when (dir) {
            0 -> left
            1 -> down
            2 -> right
            3 -> up
            else -> null
        }
    }

    fun setContainerNeighbors(oldNeighbors: Array<MathNode?>) {
        val ch = children ?: return
        val lastChild = ch.lastOrNull() ?: return
        val stop = lastChild.right

        for (dir in listOf(1, 3)) {
            val nb = oldNeighbors[dir] ?: continue
            var element: MathNode? = this
            while (element != null && element !== stop) {
                if (element.getNeighbor(dir) == null) {
                    element.setNeighbor(dir, nb)
                }
                element = element.right
            }
        }
    }
}
