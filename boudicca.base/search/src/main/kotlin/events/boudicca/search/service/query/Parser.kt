package events.boudicca.search.service.query

import java.math.BigDecimal

class Parser(private val tokens: List<Token>) {
    private var i = 0
    private var groupDepth = 0
    private var lastExpression: Expression? = null
    fun parse(): Expression {
        parseExpression()
        if (groupDepth > 0) {
            throw IllegalStateException("not all groups are closed!")
        }
        return lastExpression ?: throw IllegalStateException("could not parse any expressions?")
    }

    private fun parseExpression() {
        if (i == tokens.size) {
            throw IllegalStateException("expecting expression but encountered end of tokens")
        }
        val token = tokens[i]
        when (token.getType()) {
            TokenType.TEXT -> parseFieldAndTextExpression()
            TokenType.NOT -> parseNotExpression()
            TokenType.GROUPING_OPEN -> parseGroupOpen()
            TokenType.BEFORE, TokenType.AFTER, TokenType.IS, TokenType.DURATIONLONGER, TokenType.DURATIONSHORTER -> {
                parseSingleFieldExpression(token)
            }

            else -> throw IllegalStateException("unexpected token ${token.getType()} at start of expression at index $i")
        }
        if (i != tokens.size) {
            val trailingToken = tokens[i]
            when (trailingToken.getType()) {
                TokenType.AND, TokenType.OR -> parseBooleanExpression()
                TokenType.GROUPING_CLOSE -> parseGroupClosed()
                else -> throw IllegalStateException("unexpected token ${trailingToken.getType()} after end of expression at index $i")
            }
        }
    }

    private fun parseSingleFieldExpression(token: Token) {
        if (i + 1 >= tokens.size) {
            throw IllegalStateException("expecting date, found end of query")
        }
        val textToken = getText(i + 1)
        lastExpression = when (token.getType()) {
            TokenType.BEFORE -> BeforeExpression(textToken.getToken()!!)
            TokenType.AFTER -> AfterExpression(textToken.getToken()!!)
            TokenType.IS -> IsExpression(textToken.getToken()!!)
            TokenType.DURATIONSHORTER -> DurationShorterExpression(parseNumber(textToken.getToken()!!))
            TokenType.DURATIONLONGER -> DurationLongerExpression(parseNumber(textToken.getToken()!!))
            else -> throw IllegalStateException("unknown token type ${token.getType()}")
        }
        i += 2
    }

    private fun parseNumber(token: String): Number {
        return BigDecimal(token)
    }

    private fun parseGroupOpen() {
        groupDepth++
        i++
        parseExpression()
    }

    private fun parseGroupClosed() {
        groupDepth--
        if (groupDepth < 0) {
            throw IllegalStateException("closing non-existing group at index $i")
        }
        i++
    }

    private fun parseBooleanExpression() {
        val token = tokens[i]
        val savedLastExpression = this.lastExpression!!
        i++
        parseExpression()
        lastExpression = when (token.getType()) {
            TokenType.AND -> AndExpression(savedLastExpression, lastExpression!!)
            TokenType.OR -> OrExpression(savedLastExpression, lastExpression!!)
            else -> throw IllegalStateException("unknown token type ${token.getType()}")
        }
    }

    private fun parseNotExpression() {
        i++
        parseExpression()
        lastExpression = NotExpression(lastExpression!!)
    }

    private fun parseFieldAndTextExpression() {
        if (i + 2 >= tokens.size) {
            throw IllegalStateException("trying to parse text expression and needing 3 arguments, but there are not enough at index $i")
        }
        val fieldName = getText(i)
        val expression = getFieldAndTextExpression(i + 1)
        val text = getText(i + 2)

        lastExpression = when (expression.getType()) {
            TokenType.CONTAINS -> ContainsExpression(fieldName.getToken()!!, text.getToken()!!)
            TokenType.EQUALS -> EqualsExpression(fieldName.getToken()!!, text.getToken()!!)
            else -> throw IllegalStateException("unknown token type ${expression.getType()}")
        }
        i += 3
    }

    private fun getFieldAndTextExpression(i: Int): Token {
        val token = tokens[i]
        if (token.getType() != TokenType.CONTAINS && token.getType() != TokenType.EQUALS) {
            throw IllegalStateException("expecting text expression token at index $i but was ${token.getType()}")
        }
        return token
    }

    private fun getText(i: Int): Token {
        val token = tokens[i]
        if (token.getType() != TokenType.TEXT) {
            throw IllegalStateException("expecting text token at index $i but was ${token.getType()}")
        }
        return token
    }
}
