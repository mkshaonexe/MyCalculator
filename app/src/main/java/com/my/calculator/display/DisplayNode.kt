package com.my.calculator.display

sealed interface DisplayNode {
    data class Row(val children: List<DisplayNode>) : DisplayNode
    data class Text(val text: String, val italic: Boolean = false, val isSmall: Boolean = false) : DisplayNode
    data class Frac(val top: DisplayNode, val bottom: DisplayNode) : DisplayNode
    data class Sqrt(val radicand: DisplayNode) : DisplayNode
    data class NthRoot(val index: DisplayNode, val radicand: DisplayNode) : DisplayNode
    data class Pow(val base: DisplayNode, val exp: DisplayNode) : DisplayNode
    data class SubScript(val content: DisplayNode) : DisplayNode
    data class SupScript(val content: DisplayNode) : DisplayNode
    data class Integral(val eq: DisplayNode, val lower: DisplayNode, val upper: DisplayNode) : DisplayNode
    data class Derivative(val eq: DisplayNode, val at: DisplayNode) : DisplayNode
    object Placeholder : DisplayNode
    object Cursor : DisplayNode
    object LineBreak : DisplayNode
}
