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
        if (token.getType() == TokenType.TEXT) {
            parseFieldAndTextExpression()
        } else if (token.getType() == TokenType.NOT) {
            parseNotExpression()
        } else if (token.getType() == TokenType.GROUPING_OPEN) {
            parseGroupOpen()
        } else if (token.getType() == TokenType.BEFORE || token.getType() == TokenType.AFTER || token.getType() == TokenType.IS || token.getType() == TokenType.DURATIONLONGER || token.getType() == TokenType.DURATIONSHORTER) {
            parseSingleFieldExpression(token)
        } else {
            throw IllegalStateException("unexpected token ${token.getType()} at start of expression at index $i")
        }
        if (i != tokens.size) {
            val token = tokens[i]
            if (token.getType() == TokenType.AND || token.getType() == TokenType.OR) {
                parseBooleanExpression()
            } else if (token.getType() == TokenType.GROUPING_CLOSE) {
                parseGroupClosed()
            } else {
                throw IllegalStateException("unexpected token ${token.getType()} after end of expression at index $i")
            }
        }
    }

    private fun parseSingleFieldExpression(token: Token) {
        if (i + 1 >= tokens.size) {
            throw IllegalStateException("expecting date, found end of query")
        }
        val textToken = getText(i + 1)
        if (token.getType() == TokenType.BEFORE) {
            lastExpression = BeforeExpression(textToken.getToken()!!)
        } else if (token.getType() == TokenType.AFTER) {
            lastExpression = AfterExpression(textToken.getToken()!!)
        } else if (token.getType() == TokenType.IS) {
            lastExpression = IsExpression(textToken.getToken()!!)
        } else if (token.getType() == TokenType.DURATIONSHORTER) {
            lastExpression = DurationShorterExpression(parseNumber(textToken.getToken()!!))
        } else if (token.getType() == TokenType.DURATIONLONGER) {
            lastExpression = DurationLongerExpression(parseNumber(textToken.getToken()!!))
        } else {
            throw IllegalStateException("unknown token type ${token.getType()}")
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
        if (token.getType() == TokenType.AND) {
            lastExpression = AndExpression(savedLastExpression, lastExpression!!)
        } else if (token.getType() == TokenType.OR) {
            lastExpression = OrExpression(savedLastExpression, lastExpression!!)
        } else {
            throw IllegalStateException("unknown token type ${token.getType()}")
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

        if (expression.getType() == TokenType.CONTAINS) {
            lastExpression = ContainsExpression(fieldName.getToken()!!, text.getToken()!!)
        } else if (expression.getType() == TokenType.EQUALS) {
            lastExpression = EqualsExpression(fieldName.getToken()!!, text.getToken()!!)
        } else {
            throw IllegalStateException("unknown token type ${expression.getType()}")
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
