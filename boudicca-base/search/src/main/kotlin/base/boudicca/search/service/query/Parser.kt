package base.boudicca.search.service.query

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
            TokenType.TEXT, TokenType.BEFORE, TokenType.AFTER -> parseFieldAndTextExpression()
            TokenType.NOT -> parseNotExpression()
            TokenType.GROUPING_OPEN -> parseGroupOpen()
            TokenType.DURATION -> parseDurationExpression(token)

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

    private fun parseDurationExpression(token: Token) {
        if (i + 4 >= tokens.size) {
            throw IllegalStateException("expecting duration query, found end of query")
        }
        val startDateField = getText(i + 1)
        val endDateField = getText(i + 2)
        val shorterOrLonger = tokens[i + 3]
        val duration = parseNumber(getText(i + 4).getToken()!!)
        lastExpression = when (shorterOrLonger.getType()) {
            TokenType.LONGER -> DurationLongerExpression(
                startDateField.getToken()!!,
                endDateField.getToken()!!,
                duration
            )

            TokenType.SHORTER -> DurationShorterExpression(
                startDateField.getToken()!!,
                endDateField.getToken()!!,
                duration
            )

            else -> throw IllegalStateException("unknown token type ${token.getType()}")
        }
        i += 5
    }

    private fun parseNumber(token: String): Number {
        try {
            return BigDecimal(token)
        } catch (e: NumberFormatException) {
            throw IllegalStateException("error parsing expected number", e)
        }
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
        val expression = tokens[i + 1]
        val text = getText(i + 2)

        lastExpression = when (expression.getType()) {
            TokenType.CONTAINS -> ContainsExpression(fieldName.getToken()!!, text.getToken()!!)
            TokenType.EQUALS -> EqualsExpression(fieldName.getToken()!!, text.getToken()!!)
            TokenType.AFTER -> AfterExpression(fieldName.getToken()!!, text.getToken()!!)
            TokenType.BEFORE -> BeforeExpression(fieldName.getToken()!!, text.getToken()!!)
            else -> throw IllegalStateException("unknown token type ${expression.getType()}")
        }
        i += 3
    }

    private fun getText(i: Int): Token {
        val token = tokens[i]
        if (token.getType() != TokenType.TEXT) {
            throw IllegalStateException("expecting text token at index $i but was ${token.getType()}")
        }
        return token
    }
}
