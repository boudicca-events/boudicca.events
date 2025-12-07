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
        return if (getCurrentTokenType() == TokenType.NOT) {
            i++
            NotExpression(parseFieldExpression())
        } else {
            parseFieldExpression()
        }
    }

    //FieldExpression = grouping_open Expression grouping_close |  <all other terminal operators>
    private fun parseFieldExpression(): Expression {
        return when (getCurrentTokenType()) {
            TokenType.GROUPING_OPEN -> parseGrouping()
            TokenType.TEXT -> parseTextExpression()
            TokenType.DURATION -> parseDuration()
            TokenType.HAS_FIELD -> parseHasField()
            else -> throw QueryException("invalid token ${getCurrentTokenType()}")
        }
    }

    private fun parseDuration(): AbstractDurationExpression {
        i++
        val startField = checkText()
        val endField = checkText()
        return when (getCurrentTokenType()) {
            TokenType.LONGER -> {
                i++
                DurationLongerExpression(startField, endField, checkNumber())
            }

            TokenType.SHORTER -> {
                i++
                DurationShorterExpression(startField, endField, checkNumber())
            }

            else -> throw QueryException("invalid duration mode ${getCurrentTokenType()}, expected longer or shorter")
        }
    }

    private fun parseTextExpression(): Expression {
        val firstText = checkText()
        return when (getCurrentTokenType()) {
            TokenType.CONTAINS -> {
                i++
                ContainsExpression(firstText, checkText())
            }

            TokenType.EQUALS -> {
                i++
                EqualsExpression(firstText, checkText())
            }

            TokenType.BEFORE -> {
                i++
                BeforeExpression(firstText, checkText())
            }

            TokenType.AFTER -> {
                i++
                AfterExpression(firstText, checkText())
            }

            TokenType.IS_IN_NEXT_SECONDS -> {
                i++
                IsInNextSecondsExpression(firstText, checkNumber())
            }

            TokenType.IS_IN_LAST_SECONDS -> {
                i++
                IsInLastSecondsExpression(firstText, checkNumber())
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
        return if (i >= tokens.size) null
        else tokens[i].getType()
    }
}
