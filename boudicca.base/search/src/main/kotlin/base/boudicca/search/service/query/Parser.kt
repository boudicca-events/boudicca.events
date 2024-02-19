package base.boudicca.search.service.query

import java.math.BigDecimal

class Parser(private val tokens: List<Token>) {
    private var i = 0

    fun parse(): Expression {
        val expression = parseExpression()
        check(null)
        return expression
    }

    //Expression = AndExpression { or AndExpression }
    private fun parseExpression(): Expression {
        var expression = parseAndExpression()

        while (getCurrentTokenType() == TokenType.OR) {
            i++
            expression = OrExpression(expression, parseAndExpression())
        }

        return expression
    }

    //AndExpression = NotExpression { and NotExpression }
    private fun parseAndExpression(): Expression {
        var expression = parseNotExpression()

        while (getCurrentTokenType() == TokenType.AND) {
            i++
            expression = AndExpression(expression, parseNotExpression())
        }

        return expression
    }

    //NotExpression = [ not ] FieldExpression
    private fun parseNotExpression(): Expression {
        if (getCurrentTokenType() == TokenType.NOT) {
            i++
            return NotExpression(parseFieldExpression())
        } else {
            return parseFieldExpression()
        }
    }

    //FieldExpression = grouping_open Expression grouping_close |  <all other terminal operators>
    private fun parseFieldExpression(): Expression {
        when (getCurrentTokenType()) {
            TokenType.GROUPING_OPEN -> {
                return parseGrouping()
            }

            TokenType.TEXT -> {
                return parseTextExpression()
            }

            TokenType.DURATION -> {
                return parseDuration()
            }

            else -> throw IllegalStateException("invalid token ${getCurrentTokenType()}")
        }
    }

    private fun parseDuration(): AbstractDurationExpression {
        i++
        val startField = checkText()
        val endField = checkText()
        when (getCurrentTokenType()) {
            TokenType.LONGER -> {
                i++
                return DurationLongerExpression(startField, endField, checkNumber())
            }

            TokenType.SHORTER -> {
                i++
                return DurationShorterExpression(startField, endField, checkNumber())
            }

            else -> throw IllegalStateException("invalid duration mode ${getCurrentTokenType()}, expected longer or shorter")
        }
    }

    private fun parseTextExpression(): FieldAndTextExpression {
        val firstText = checkText()
        when (getCurrentTokenType()) {
            TokenType.CONTAINS -> {
                i++
                return ContainsExpression(firstText, checkText())
            }

            TokenType.EQUALS -> {
                i++
                return EqualsExpression(firstText, checkText())
            }

            TokenType.BEFORE -> {
                i++
                return BeforeExpression(firstText, checkText())
            }

            TokenType.AFTER -> {
                i++
                return AfterExpression(firstText, checkText())
            }

            else -> throw IllegalStateException("invalid token ${getCurrentTokenType()} following after text token")
        }
    }

    private fun parseGrouping(): Expression {
        i++
        val expression = parseExpression()
        check(TokenType.GROUPING_CLOSE)
        return expression
    }

    private fun checkNumber(): Number {
        try {
            return BigDecimal(checkText())
        } catch (e: NumberFormatException) {
            throw IllegalStateException("error parsing expected number", e)
        }
    }

    private fun checkText(): String {
        check(TokenType.TEXT)
        val token = tokens[i - 1]
        return token.getToken()!!
    }

    private fun check(tokenType: TokenType?) {
        val currentTokenType = getCurrentTokenType()
        if (currentTokenType != tokenType) {
            throw IllegalStateException("did expect token ${tokenType ?: "eof"} but got token ${currentTokenType ?: "eof"}")
        }
        i++
    }

    private fun getCurrentTokenType(): TokenType? {
        if (i >= tokens.size) {
            return null
        }
        return tokens[i].getType()
    }
}
