package base.boudicca.search.query

import base.boudicca.search.service.query.Lexer
import base.boudicca.search.service.query.Token
import base.boudicca.search.service.query.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class LexerTest {

    @Test
    fun testEmpty() {
        callLexer("""  """).isEmpty()
    }

    @Test
    fun singleTextToken() {
        val tokens = callLexer(""" "text" """)
        assertEquals(1, tokens.size)
        assertEquals(tokens[0].getType(), TokenType.TEXT)
        assertEquals(tokens[0].getToken(), "text")
    }

    @Test
    fun singleTextTokenWithNumber() {
        val tokens = callLexer(""" "1text" """)
        assertEquals(1, tokens.size)
        assertEquals(tokens[0].getType(), TokenType.TEXT)
        assertEquals(tokens[0].getToken(), "1text")
    }

    @Test
    fun twoTextTokens() {
        val tokens = callLexer(""" "first" "second" """)
        assertEquals(2, tokens.size)
        assertEquals(tokens[0].getType(), TokenType.TEXT)
        assertEquals(tokens[0].getToken(), "first")
        assertEquals(tokens[1].getType(), TokenType.TEXT)
        assertEquals(tokens[1].getToken(), "second")
    }

    @Test
    fun testAndToken() {
        val tokens = callLexer(""" and AND aNd """)
        assertEquals(3, tokens.size)
        assertEquals(TokenType.AND, tokens[0].getType())
        assertEquals(TokenType.AND, tokens[1].getType())
        assertEquals(TokenType.AND, tokens[2].getType())
    }

    @Test
    fun testOrToken() {
        val tokens = callLexer(""" or OR oR """)
        assertEquals(3, tokens.size)
        assertEquals(TokenType.OR, tokens[0].getType())
        assertEquals(TokenType.OR, tokens[1].getType())
        assertEquals(TokenType.OR, tokens[2].getType())
    }

    @Test
    fun testNotToken() {
        val tokens = callLexer(""" not NOT nOT """)
        assertEquals(3, tokens.size)
        assertEquals(TokenType.NOT, tokens[0].getType())
        assertEquals(TokenType.NOT, tokens[1].getType())
        assertEquals(TokenType.NOT, tokens[2].getType())
    }

    @Test
    fun testEqualsToken() {
        val tokens = callLexer(""" equals EQUALS equals """)
        assertEquals(3, tokens.size)
        assertEquals(TokenType.EQUALS, tokens[0].getType())
        assertEquals(TokenType.EQUALS, tokens[1].getType())
        assertEquals(TokenType.EQUALS, tokens[2].getType())
    }

    @Test
    fun testContainsToken() {
        val tokens = callLexer(""" contains CONTAINS ConTAIns """)
        assertEquals(3, tokens.size)
        assertEquals(TokenType.CONTAINS, tokens[0].getType())
        assertEquals(TokenType.CONTAINS, tokens[1].getType())
        assertEquals(TokenType.CONTAINS, tokens[2].getType())
    }

    @Test
    fun testGroupingToken() {
        val tokens = callLexer(""" ( ) """)
        assertEquals(2, tokens.size)
        assertEquals(TokenType.GROUPING_OPEN, tokens[0].getType())
        assertEquals(TokenType.GROUPING_CLOSE, tokens[1].getType())
    }

    @Test
    fun testSimpleEscapedText() {
        val tokens = callLexer(""" "text" """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.TEXT, tokens[0].getType())
        assertEquals("text", tokens[0].getToken())
    }

    @Test
    fun testEscapedTextWithSpecialChars() {
        val tokens = callLexer(""" "text with space and 123 and ?!§$ and 😘" """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.TEXT, tokens[0].getType())
        assertEquals("text with space and 123 and ?!§$ and 😘", tokens[0].getToken())
    }

    @Test
    fun testEscapedTextWithEscapedChars() {
        val tokens = callLexer(""" "text with \" and \\ " """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.TEXT, tokens[0].getType())
        assertEquals("text with \" and \\ ", tokens[0].getToken())
    }

    @Test
    fun testEscapedOperator() {
        val tokens = callLexer(""" "and" """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.TEXT, tokens[0].getType())
        assertEquals("and", tokens[0].getToken())
    }

    @Test
    fun testAfterOperator() {
        val tokens = callLexer(""" after """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.AFTER, tokens[0].getType())
    }

    @Test
    fun testBeforeOperator() {
        val tokens = callLexer(""" before """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.BEFORE, tokens[0].getType())
    }

    @Test
    fun testVariousErrors() {
        assertThrows<IllegalStateException> {
            callLexer(""" textwith!init """)
        }
        assertThrows<IllegalStateException> {
            callLexer(""" \"unclosed quotes """)
        }
        assertThrows<IllegalStateException> {
            callLexer(""" \"escaped end\\ """)
        }
        assertThrows<IllegalStateException> {
            callLexer(""" "wrongly escaped \a " """)
        }
        assertThrows<IllegalStateException> {
            callLexer(""" invalidtoken """)
        }
        assertThrows<IllegalStateException> {
            callLexer(""" 5invalidnumber """)
        }
        assertThrows<IllegalStateException> {
            callLexer(""" -5invalidnumber """)
        }
        assertThrows<IllegalStateException> {
            callLexer(""" & """)
        }
        assertThrows<IllegalStateException> {
            callLexer(""" a&nd """)
        }
    }

    @Test
    fun testLongMixedQuery() {
        val tokens =
            callLexer(""" ("field" contains "and") and ( "field2" equals "long text" ) or not "field" contains "text" """)
        assertEquals(16, tokens.size)
        assertEquals(TokenType.GROUPING_OPEN, tokens[0].getType())
        assertEquals(TokenType.TEXT, tokens[1].getType())
        assertEquals("field", tokens[1].getToken())
        assertEquals(TokenType.CONTAINS, tokens[2].getType())
        assertEquals(TokenType.TEXT, tokens[3].getType())
        assertEquals("and", tokens[3].getToken())
        assertEquals(TokenType.GROUPING_CLOSE, tokens[4].getType())
        assertEquals(TokenType.AND, tokens[5].getType())
        assertEquals(TokenType.GROUPING_OPEN, tokens[6].getType())
        assertEquals(TokenType.TEXT, tokens[7].getType())
        assertEquals("field2", tokens[7].getToken())
        assertEquals(TokenType.EQUALS, tokens[8].getType())
        assertEquals(TokenType.TEXT, tokens[9].getType())
        assertEquals("long text", tokens[9].getToken())
        assertEquals(TokenType.GROUPING_CLOSE, tokens[10].getType())
        assertEquals(TokenType.OR, tokens[11].getType())
        assertEquals(TokenType.NOT, tokens[12].getType())
        assertEquals(TokenType.TEXT, tokens[13].getType())
        assertEquals("field", tokens[13].getToken())
        assertEquals(TokenType.CONTAINS, tokens[14].getType())
        assertEquals(TokenType.TEXT, tokens[15].getType())
        assertEquals("text", tokens[15].getToken())
    }

    @Test
    fun testDurationOperator() {
        val tokens = callLexer(""" duration """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.DURATION, tokens[0].getType())
    }
    @Test
    fun testShorterKeyword() {
        val tokens = callLexer(""" shorter """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.SHORTER, tokens[0].getType())
    }
    @Test
    fun testLongerKeyword() {
        val tokens = callLexer(""" longer """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.LONGER, tokens[0].getType())
    }

    @Test
    fun testSimpleNumber() {
        val tokens = callLexer(""" 2 """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.NUMBER, tokens[0].getType())
        assertEquals(BigDecimal(2), tokens[0].getNumber())
    }

    @Test
    fun testNegativeNumber() {
        val tokens = callLexer(""" -2 """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.NUMBER, tokens[0].getType())
        assertEquals(BigDecimal(-2), tokens[0].getNumber())
    }

    @Test
    fun testFractalNumber() {
        val tokens = callLexer(""" -2.5 """)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.NUMBER, tokens[0].getType())
        assertEquals(BigDecimal(-2.5), tokens[0].getNumber())
    }

    private fun callLexer(query: String): List<Token> {
        return Lexer(query).lex()
    }
}