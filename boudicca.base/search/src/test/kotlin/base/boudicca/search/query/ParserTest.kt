package base.boudicca.search.query

import base.boudicca.search.service.query.Parser
import base.boudicca.search.service.query.Token
import base.boudicca.search.service.query.TokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserTest {

    @Test
    fun testContains() {
        assertEquals(
            "CONTAINS('field','text')",
            callParser(
                text("field"), contains(), text("text")
            )
        )
    }

    @Test
    fun testEquals() {
        assertEquals(
            "EQUALS('field','text')",
            callParser(
                text("field"), equals(), text("text")
            )
        )
    }

    @Test
    fun testNotEquals() {
        assertEquals(
            "NOT(EQUALS('field','text'))",
            callParser(
                not(), text("field"), equals(), text("text")
            )
        )
    }

    @Test
    fun testAndEquals() {
        assertEquals(
            "AND(EQUALS('field','text'),EQUALS('field2','text2'))",
            callParser(
                text("field"), equals(), text("text"), and(), text("field2"), equals(), text("text2")
            )
        )
    }

    @Test
    fun testOrEquals() {
        assertEquals(
            "OR(EQUALS('field','text'),EQUALS('field2','text2'))",
            callParser(
                text("field"), equals(), text("text"), or(), text("field2"), equals(), text("text2")
            )
        )
    }

    @Test
    fun testNotOrEquals() {
        assertEquals(
            "OR(NOT(EQUALS('field','text')),EQUALS('field2','text2'))",
            callParser(
                not(), text("field"), equals(), text("text"), or(), text("field2"), equals(), text("text2")
            )
        )
        assertEquals(
            "OR(NOT(EQUALS('field','text')),NOT(EQUALS('field2','text2')))",
            callParser(
                not(), text("field"), equals(), text("text"), or(), not(), text("field2"), equals(), text("text2")
            )
        )
    }

    @Test
    fun testGrouping() {
        assertEquals(
            "EQUALS('field','text')",
            callParser(
                grOpen(), text("field"), equals(), text("text"), grClose()
            )
        )
    }

    @Test
    fun testDoubleGrouping() {
        assertEquals(
            "EQUALS('field','text')",
            callParser(
                grOpen(), grOpen(), text("field"), equals(), text("text"), grClose(), grClose()
            )
        )
    }

    @Test
    fun testGroupingWithOperators() {
        assertEquals(
            "OR(AND(EQUALS('field','text'),EQUALS('field2','text2')),AND(EQUALS('field3','text3'),EQUALS('field4','text4')))",
            callParser(
                grOpen(),
                text("field"), equals(), text("text"),
                and(),
                text("field2"), equals(), text("text2"),
                grClose(),
                or(),
                grOpen(),
                text("field3"), equals(), text("text3"),
                and(),
                text("field4"), equals(), text("text4"),
                grClose(),
            )
        )
    }

    @Test
    fun testGroupingWithNot() {
        assertEquals(
            "OR(NOT(EQUALS('field','text')),NOT(EQUALS('field2','text2')))",
            callParser(
                grOpen(), not(), text("field"), equals(), text("text"), grClose(),
                or(),
                grOpen(), not(), text("field2"), equals(), text("text2"), grClose()
            )
        )
    }

    @Test
    fun testVariousErrors() {
        assertThrows<IllegalStateException> {
            //non closed group
            callParser(grOpen(), text("field"), equals(), text("text"))
        }
        assertThrows<IllegalStateException> {
            //non closed group
            callParser(grOpen(), grOpen(), text("field"), equals(), text("text"), grClose())
        }
        assertThrows<IllegalStateException> {
            //too many closing group
            callParser(grOpen(), text("field"), equals(), text("text"), grClose(), grClose())
        }
        assertThrows<IllegalStateException> {
            //illegal place for closing group
            callParser(grOpen(), text("field"), grClose(), equals(), text("text"))
        }
        assertThrows<IllegalStateException> {
            //empty group is illegal
            callParser(grOpen(), grClose())
        }
        assertThrows<IllegalStateException> {
            //invalid place for opening group
            callParser(text("field"), grOpen(), equals(), text("text"), grClose())
        }
        assertThrows<IllegalStateException> {
            //not enough tokens
            callParser(text("field"), equals())
        }
        assertThrows<IllegalStateException> {
            //wrong middle token type
            callParser(text("field"), text("equals"), text("text"))
        }
        assertThrows<IllegalStateException> {
            //boolean operator without second expression
            callParser(text("field"), equals(), text("text"), and())
        }
        assertThrows<IllegalStateException> {
            //boolean operator without first expression
            callParser(and(), text("field"), equals(), text("text"))
        }
        assertThrows<IllegalStateException> {
            //after operator needs parameter
            callParser(after())
        }
        assertThrows<IllegalStateException> {
            //wrong date format
            callParser(after(), text("27-5-2023"))
        }
    }

    @Test
    fun testBefore() {
        assertEquals(
            "BEFORE('startDate','2023-05-27')",
            callParser(
                text("startDate"), before(), text("2023-05-27")
            )
        )
    }

    @Test
    fun testAfter() {
        assertEquals(
            "AFTER('startDate','2023-05-27')",
            callParser(
                text("startDate"), after(), text("2023-05-27")
            )
        )
    }

    @Test
    fun testDurationShorter() {
        assertEquals(
            "DURATIONSHORTER('startDate','endDate',-2)",
            callParser(
                duration(), text("startDate"), text("endDate"), shorter(), text("-2")
            )
        )
    }

    @Test
    fun testDurationLonger() {
        assertEquals(
            "DURATIONLONGER('startDate','endDate',2.6)",
            callParser(
                duration(), text("startDate"), text("endDate"), longer(), text("2.6")
            )
        )
    }

    private fun before(): Token {
        return Token(TokenType.BEFORE, null)
    }

    private fun after(): Token {
        return Token(TokenType.AFTER, null)
    }

    private fun grOpen(): Token {
        return Token(TokenType.GROUPING_OPEN, null)
    }

    private fun grClose(): Token {
        return Token(TokenType.GROUPING_CLOSE, null)
    }

    private fun not(): Token {
        return Token(TokenType.NOT, null)
    }

    private fun or(): Token {
        return Token(TokenType.OR, null)
    }

    private fun and(): Token {
        return Token(TokenType.AND, null)
    }

    private fun equals(): Token {
        return Token(TokenType.EQUALS, null)
    }

    private fun contains(): Token {
        return Token(TokenType.CONTAINS, null)
    }

    private fun text(s: String): Token {
        return Token(TokenType.TEXT, s)
    }

    private fun duration(): Token {
        return Token(TokenType.DURATION, null)
    }

    private fun shorter(): Token {
        return Token(TokenType.SHORTER, null)
    }

    private fun longer(): Token {
        return Token(TokenType.LONGER, null)
    }

    private fun callParser(vararg tokens: Token): String {
        return Parser(tokens.toList()).parse().toString()
    }
}