package com.my.calculator.core.math

class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): AstNode {
        val expr = parseExpression()
        if (!isAtEnd()) {
            val t = peek()
            throw IllegalArgumentException("Unexpected token '${t.text}' at position ${t.position}")
        }
        return expr
    }

    private fun parseExpression(): AstNode = parseAdditive()

    private fun parseAdditive(): AstNode {
        var expr = parseMultiplicative()
        while (!isAtEnd()) {
            expr = when {
                match(TokenType.PLUS) -> BinaryOpNode(BinaryOp.ADD, expr, parseMultiplicative())
                match(TokenType.MINUS) -> BinaryOpNode(BinaryOp.SUBTRACT, expr, parseMultiplicative())
                else -> break
            }
        }
        return expr
    }

    private fun parseMultiplicative(): AstNode {
        var expr = parseUnaryMinus()
        while (!isAtEnd()) {
            if (match(TokenType.STAR)) {
                expr = BinaryOpNode(BinaryOp.MULTIPLY, expr, parseUnaryMinus())
            } else if (match(TokenType.SLASH)) {
                expr = BinaryOpNode(BinaryOp.DIVIDE, expr, parseUnaryMinus())
            } else if (canStartPrimary(peek())) {
                // Implicit multiplication
                expr = BinaryOpNode(BinaryOp.MULTIPLY, expr, parseUnaryMinus())
            } else {
                break
            }
        }
        return expr
    }

    private fun parseUnaryMinus(): AstNode {
        return when {
            match(TokenType.MINUS) -> UnaryOpNode(UnaryOp.NEGATE, parseUnaryMinus())
            match(TokenType.PLUS) -> parseUnaryMinus()
            else -> parsePower()
        }
    }

    private fun parsePower(): AstNode {
        val expr = parsePostfix()
        if (match(TokenType.CARET)) {
            // Right-associative power
            val right = parseUnaryMinus()
            return BinaryOpNode(BinaryOp.POWER, expr, right)
        }
        return expr
    }

    private fun parsePostfix(): AstNode {
        var expr = parsePrimary()
        while (match(TokenType.BANG)) {
            expr = UnaryOpNode(UnaryOp.FACTORIAL, expr)
        }
        return expr
    }

    private fun parsePrimary(): AstNode {
        val t = advance()
        return when (t.type) {
            TokenType.NUMBER -> NumberNode(t.numberValue)
            TokenType.PLACEHOLDER -> PlaceholderNode(t.text)
            TokenType.IDENTIFIER -> {
                if (match(TokenType.LPAREN)) {
                    val args = mutableListOf<AstNode>()
                    if (!check(TokenType.RPAREN)) {
                        do {
                            args.add(parseExpression())
                        } while (match(TokenType.COMMA))
                    }
                    consume(TokenType.RPAREN, "Expected ')' after function arguments")
                    FunctionCallNode(t.text, args)
                } else {
                    IdentifierNode(t.text)
                }
            }
            TokenType.LPAREN -> {
                val expr = parseExpression()
                consume(TokenType.RPAREN, "Expected ')' after expression")
                expr
            }
            TokenType.LBRACKET -> {
                val inner = parseExpression()
                consume(TokenType.RBRACKET, "Expected ']' after expression")
                var result: AstNode = inner
                while (match(TokenType.LBRACKET)) {
                    val index = parseExpression()
                    consume(TokenType.RBRACKET, "Expected ']' after index")
                    result = MatrixIndexNode(result, index)
                }
                result
            }
            else -> throw IllegalArgumentException("Unexpected token '${t.text}' at position ${t.position}")
        }
    }

    private fun canStartPrimary(t: Token): Boolean {
        return when (t.type) {
            TokenType.NUMBER,
            TokenType.IDENTIFIER,
            TokenType.PLACEHOLDER,
            TokenType.LPAREN,
            TokenType.LBRACKET -> true
            else -> false
        }
    }

    private fun match(type: TokenType): Boolean {
        if (check(type)) {
            advance()
            return true
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return type == TokenType.EOF
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = current >= tokens.size || peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        val pos = if (current < tokens.size) peek().position else -1
        throw IllegalArgumentException("$message at position $pos")
    }
}
