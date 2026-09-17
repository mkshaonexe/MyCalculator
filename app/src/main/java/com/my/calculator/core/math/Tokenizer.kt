package com.my.calculator.core.math

enum class TokenType {
    NUMBER,
    IDENTIFIER,
    PLACEHOLDER,
    PLUS,
    MINUS,
    STAR,
    SLASH,
    CARET,
    BANG,
    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    COMMA,
    EOF
}

data class Token(
    val type: TokenType,
    val text: String,
    val numberValue: Double = 0.0,
    val position: Int = 0
)

class Tokenizer(private val input: String) {
    private var pos = 0

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (pos < input.length) {
            val ch = input[pos]
            if (ch.isWhitespace()) {
                pos++
                continue
            }

            val startPos = pos
            when {
                ch.isDigit() || (ch == '.' && pos + 1 < input.length && input[pos + 1].isDigit()) -> {
                    tokens.add(readNumber())
                }
                ch == '@' -> {
                    tokens.add(readPlaceholder())
                }
                ch.isLetter() || ch == '_' -> {
                    tokens.add(readIdentifier())
                }
                ch == '+' -> { pos++; tokens.add(Token(TokenType.PLUS, "+", position = startPos)) }
                ch == '-' -> { pos++; tokens.add(Token(TokenType.MINUS, "-", position = startPos)) }
                ch == '*' -> { pos++; tokens.add(Token(TokenType.STAR, "*", position = startPos)) }
                ch == '/' -> { pos++; tokens.add(Token(TokenType.SLASH, "/", position = startPos)) }
                ch == '^' -> { pos++; tokens.add(Token(TokenType.CARET, "^", position = startPos)) }
                ch == '!' -> { pos++; tokens.add(Token(TokenType.BANG, "!", position = startPos)) }
                ch == '(' -> { pos++; tokens.add(Token(TokenType.LPAREN, "(", position = startPos)) }
                ch == ')' -> { pos++; tokens.add(Token(TokenType.RPAREN, ")", position = startPos)) }
                ch == '[' -> { pos++; tokens.add(Token(TokenType.LBRACKET, "[", position = startPos)) }
                ch == ']' -> { pos++; tokens.add(Token(TokenType.RBRACKET, "]", position = startPos)) }
                ch == ',' -> { pos++; tokens.add(Token(TokenType.COMMA, ",", position = startPos)) }
                else -> {
                    throw IllegalArgumentException("Unexpected character '$ch' at position $pos in \"$input\"")
                }
            }
        }
        tokens.add(Token(TokenType.EOF, "", position = pos))
        return tokens
    }

    private fun readNumber(): Token {
        val start = pos
        while (pos < input.length && input[pos].isDigit()) pos++
        if (pos < input.length && input[pos] == '.') {
            pos++
            while (pos < input.length && input[pos].isDigit()) pos++
        }
        if (pos < input.length && (input[pos] == 'e' || input[pos] == 'E')) {
            val nextPos = pos + 1
            if (nextPos < input.length && (input[nextPos].isDigit() || input[nextPos] == '+' || input[nextPos] == '-')) {
                // If it's '+' or '-', make sure there's a digit after it
                if ((input[nextPos] == '+' || input[nextPos] == '-') && (nextPos + 1 >= input.length || !input[nextPos + 1].isDigit())) {
                    // Not a valid exponent
                } else {
                    pos++ // consume 'e'
                    if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) pos++
                    while (pos < input.length && input[pos].isDigit()) pos++
                }
            }
        }
        val text = input.substring(start, pos)
        val value = text.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number '$text' at position $start")
        return Token(TokenType.NUMBER, text, numberValue = value, position = start)
    }

    private fun readPlaceholder(): Token {
        val start = pos
        pos++ // skip '@'
        while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
        val text = input.substring(start, pos)
        return Token(TokenType.PLACEHOLDER, text, position = start)
    }

    private fun readIdentifier(): Token {
        val start = pos
        while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
        val text = input.substring(start, pos)
        return Token(TokenType.IDENTIFIER, text, position = start)
    }
}
