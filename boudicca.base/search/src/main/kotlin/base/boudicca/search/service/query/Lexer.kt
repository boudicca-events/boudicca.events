package base.boudicca.search.service.query

import java.math.BigDecimal


class Lexer(private val query: String) {

    private var i = 0
    private val tokens = mutableListOf<Token>()

    fun lex(): List<Token> {

        while (i < query.length) {
            val c = query[i]
            if (c == '"') {
                readEscapedTextToken()
            } else if (c == '(') {
                tokens.add(Token(TokenType.GROUPING_OPEN, null))
                i++
            } else if (c == ')') {
                tokens.add(Token(TokenType.GROUPING_CLOSE, null))
                i++
            } else if (c == '-' || c.isDigit()) {
                readNumber()
            } else if (c.isLetter()) {
                readToken()
            } else if (c.isWhitespace()) {
                i++
            } else {
                throw QueryException("invalid character $c at index $i")
            }
        }

        return tokens
    }

    private fun readNumber() {
        var tokenEnd = i + 1
        while (tokenEnd < query.length) {
            val c = query[tokenEnd]
            if (c == '(' || c == ')' || c.isWhitespace()) {
                break
            }
            if (!(c.isDigit() || c == '.')) {
                throw QueryException("invalid character $c found while parsing number")
            }
            tokenEnd++
        }
        val token = query.substring(i, tokenEnd)
        try {
            tokens.add(Token(TokenType.NUMBER, null, BigDecimal(token)))
        } catch (e: NumberFormatException) {
            throw QueryException("error parsing number $token", e)
        }
        i = tokenEnd
    }

    private fun readToken() {
        var tokenEnd = i
        while (tokenEnd < query.length) {
            val c = query[tokenEnd]
            if (c.isWhitespace() || c == '(' || c == ')') {
                break
            }
            if (!c.isLetter()) {
                throw QueryException("unexpected non-letter character in keyword token: $c at index: $i")
            }
            tokenEnd++
        }
        if (tokenEnd == i) {
            throw QueryException("how did i find an empty text token at place: $i ?")
        }
        val token = query.substring(i, tokenEnd)
        when (token.lowercase()) {
            "and" -> tokens.add(Token(TokenType.AND, null))
            "or" -> tokens.add(Token(TokenType.OR, null))
            "not" -> tokens.add(Token(TokenType.NOT, null))
            "equals" -> tokens.add(Token(TokenType.EQUALS, null))
            "contains" -> tokens.add(Token(TokenType.CONTAINS, null))
            "before" -> tokens.add(Token(TokenType.BEFORE, null))
            "after" -> tokens.add(Token(TokenType.AFTER, null))
            "duration" -> tokens.add(Token(TokenType.DURATION, null))
            "longer" -> tokens.add(Token(TokenType.LONGER, null))
            "shorter" -> tokens.add(Token(TokenType.SHORTER, null))
            "hasfield" -> tokens.add(Token(TokenType.HAS_FIELD, null))
            else -> throw QueryException("unknown keyword: $token (did you forget to quote your text?)")
        }
        i = tokenEnd
    }

    private fun readEscapedTextToken() {
        val sb = StringBuilder()
        i++
        while (true) {
            if (i == query.length) {
                throw QueryException("not-closed escaped text literal starting at: ${this.i}")
            }
            val c = query[i]
            if (c == '\\') {
                i++
                if (i == query.length) {
                    throw QueryException("you cannot escape the end of the query!")
                }
                val escapedC = query[i]
                if (escapedC != '\\' && escapedC != '"') {
                    throw QueryException("unknown escaped character: $escapedC")
                }
            }
            if (c == '"') {
                break
            }
            sb.append(query[i])
            i++
        }
        i++
        tokens.add(Token(TokenType.TEXT, sb.toString()))
    }
}
