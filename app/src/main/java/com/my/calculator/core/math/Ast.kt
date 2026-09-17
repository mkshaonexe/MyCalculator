package com.my.calculator.core.math

sealed interface AstNode

data class NumberNode(val value: Double) : AstNode
data class IdentifierNode(val name: String) : AstNode
data class PlaceholderNode(val key: String) : AstNode

data class UnaryOpNode(val op: UnaryOp, val expr: AstNode) : AstNode
enum class UnaryOp {
    NEGATE,
    FACTORIAL
}

data class BinaryOpNode(val op: BinaryOp, val left: AstNode, val right: AstNode) : AstNode
enum class BinaryOp {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER
}

data class FunctionCallNode(val name: String, val args: List<AstNode>) : AstNode
data class MatrixIndexNode(val target: AstNode, val index: AstNode) : AstNode
