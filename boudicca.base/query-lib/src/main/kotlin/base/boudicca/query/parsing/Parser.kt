package base.boudicca.query.parsing

import base.boudicca.query.AbstractDurationExpression
import base.boudicca.query.AfterExpression
import base.boudicca.query.AndExpression
import base.boudicca.query.BeforeExpression
import base.boudicca.query.ContainsExpression
import base.boudicca.query.DurationLongerExpression
import base.boudicca.query.DurationShorterExpression
import base.boudicca.query.EqualsExpression
import base.boudicca.query.Expression
import base.boudicca.query.HasFieldExpression
import base.boudicca.query.IsInLastSecondsExpression
import base.boudicca.query.IsInNextSecondsExpression
import base.boudicca.query.NotExpression
import base.boudicca.query.OrExpression
import base.boudicca.query.QueryException

class Parser(private val tokens: List<Token>) {
    private var i = 0

    fun parse(): Expression {
        val expression = parseExpression()
        check(null) //check eof
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

            TokenType.HAS_FIELD -> {
                return parseHasField()
            }

            else -> throw QueryException("invalid token ${getCurrentTokenType()}")
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

            else -> throw QueryException("invalid duration mode ${getCurrentTokenType()}, expected longer or shorter")
        }
    }

    private fun parseTextExpression(): Expression {
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

            TokenType.IS_IN_NEXT_SECONDS -> {
                i++
                return IsInNextSecondsExpression(firstText, checkNumber())
            }

            TokenType.IS_IN_LAST_SECONDS -> {
                i++
                return IsInLastSecondsExpression(firstText, checkNumber())
            }

            else -> throw QueryException("invalid token ${getCurrentTokenType()} following after text token")
        }
    }

    private fun parseHasField(): HasFieldExpression {
        i++
        val field = checkText()
        return HasFieldExpression(field)
    }

    private fun parseGrouping(): Expression {
        i++
        val expression = parseExpression()
        check(TokenType.GROUPING_CLOSE)
        return expression
    }


    private fun checkNumber(): Number {
        check(TokenType.NUMBER)
        val token = tokens[i - 1]
        return token.getNumber()!!
    }

    private fun checkText(): String {
        check(TokenType.TEXT)
        val token = tokens[i - 1]
        return token.getToken()!!
    }

    private fun check(tokenType: TokenType?) {
        val currentTokenType = getCurrentTokenType()
        if (currentTokenType != tokenType) {
            throw QueryException("did expect token ${tokenType ?: "eof"} but got token ${currentTokenType ?: "eof"}")
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
