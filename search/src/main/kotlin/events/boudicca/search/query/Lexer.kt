package events.boudicca.search.query


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
            } else if (!c.isWhitespace()) {
                readToken()
            } else {
                i++
            }
        }

        return tokens
    }

    private fun readToken() {
        var tokenEnd = i
        while (tokenEnd < query.length) {
            val c = query[tokenEnd]
            if (c.isWhitespace() || c == '(' || c == ')') {
                break
            }
            if (!c.isLetterOrDigit() && c != '.') {
                throw IllegalStateException("unexpected non-letter character in text token: $c at index: $i")
            }
            tokenEnd++
        }
        if (tokenEnd == i) {
            throw IllegalStateException("how did i find an empty text token at place: $i ?")
        }
        val token = query.substring(i, tokenEnd)
        when (token.lowercase()) {
            "and" -> tokens.add(Token(TokenType.AND, null))
            "or" -> tokens.add(Token(TokenType.OR, null))
            "not" -> tokens.add(Token(TokenType.NOT, null))
            "equals" -> tokens.add(Token(TokenType.EQUALS, null))
            "contains" -> tokens.add(Token(TokenType.CONTAINS, null))
            else -> tokens.add(Token(TokenType.TEXT, token))
        }
        i = tokenEnd
    }

    private fun readEscapedTextToken() {
        val sb = StringBuilder()
        i++
        while (true) {
            if (i == query.length) {
                throw IllegalStateException("not-closed escaped text literal starting at: ${this.i}")
            }
            val c = query[i]
            if (c == '\\') {
                i++
                if (i == query.length) {
                    throw IllegalStateException("you cannot escape the end of the query!")
                }
                val escapedC = query[i]
                if (escapedC != '\\' && escapedC != '"') {
                    throw IllegalStateException("unknown escaped character: $escapedC")
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
