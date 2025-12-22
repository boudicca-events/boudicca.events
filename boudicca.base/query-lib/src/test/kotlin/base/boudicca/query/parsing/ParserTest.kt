package base.boudicca.query.parsing

import base.boudicca.query.QueryException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ParserTest {
    @Test
    fun testContains() {
        assertEquals(
            "CONTAINS('field','text')",
            callParser(
                text("field"),
                contains(),
                text("text"),
            ),
        )
    }

    @Test
    fun testEquals() {
        assertEquals(
            "EQUALS('field','text')",
            callParser(
                text("field"),
                equals(),
                text("text"),
            ),
        )
    }

    @Test
    fun testNotEquals() {
        assertEquals(
            "NOT(EQUALS('field','text'))",
            callParser(
                not(),
                text("field"),
                equals(),
                text("text"),
            ),
        )
    }

    @Test
    fun testAndEquals() {
        assertEquals(
            "AND(EQUALS('field','text'),EQUALS('field2','text2'))",
            callParser(
                text("field"),
                equals(),
                text("text"),
                and(),
                text("field2"),
                equals(),
                text("text2"),
            ),
        )
    }

    @Test
    fun testOrEquals() {
        assertEquals(
            "OR(EQUALS('field','text'),EQUALS('field2','text2'))",
            callParser(
                text("field"),
                equals(),
                text("text"),
                or(),
                text("field2"),
                equals(),
                text("text2"),
            ),
        )
    }

    @Test
    fun testNotOrEquals() {
        assertEquals(
            "OR(NOT(EQUALS('field','text')),EQUALS('field2','text2'))",
            callParser(
                not(),
                text("field"),
                equals(),
                text("text"),
                or(),
                text("field2"),
                equals(),
                text("text2"),
            ),
        )
        assertEquals(
            "OR(NOT(EQUALS('field','text')),NOT(EQUALS('field2','text2')))",
            callParser(
                not(), text("field"), equals(), text("text"), or(), not(), text("field2"), equals(), text("text2"),
            ),
        )
    }

    @Test
    fun testGrouping() {
        assertEquals(
            "EQUALS('field','text')",
            callParser(
                grOpen(),
                text("field"),
                equals(),
                text("text"),
                grClose(),
            ),
        )
    }

    @Test
    fun testDoubleGrouping() {
        assertEquals(
            "EQUALS('field','text')",
            callParser(
                grOpen(),
                grOpen(),
                text("field"),
                equals(),
                text("text"),
                grClose(),
                grClose(),
            ),
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
            ),
        )
    }

    @Test
    fun testGroupingWithNot() {
        assertEquals(
            "OR(NOT(EQUALS('field','text')),NOT(EQUALS('field2','text2')))",
            callParser(
                grOpen(),
                not(),
                text("field"),
                equals(),
                text("text"),
                grClose(),
                or(),
                grOpen(),
                not(),
                text("field2"),
                equals(),
                text("text2"),
                grClose(),
            ),
        )
    }

    @Test
    fun testVariousErrors() {
        assertThrows<QueryException> {
            // non closed group
            callParser(grOpen(), text("field"), equals(), text("text"))
        }
        assertThrows<QueryException> {
            // non closed group
            callParser(grOpen(), grOpen(), text("field"), equals(), text("text"), grClose())
        }
        assertThrows<QueryException> {
            // too many closing group
            callParser(grOpen(), text("field"), equals(), text("text"), grClose(), grClose())
        }
        assertThrows<QueryException> {
            // illegal place for closing group
            callParser(grOpen(), text("field"), grClose(), equals(), text("text"))
        }
        assertThrows<QueryException> {
            // empty group is illegal
            callParser(grOpen(), grClose())
        }
        assertThrows<QueryException> {
            // invalid place for opening group
            callParser(text("field"), grOpen(), equals(), text("text"), grClose())
        }
        assertThrows<QueryException> {
            // not enough tokens
            callParser(text("field"), equals())
        }
        assertThrows<QueryException> {
            // wrong middle token type
            callParser(text("field"), text("equals"), text("text"))
        }
        assertThrows<QueryException> {
            // boolean operator without second expression
            callParser(text("field"), equals(), text("text"), and())
        }
        assertThrows<QueryException> {
            // boolean operator without first expression
            callParser(and(), text("field"), equals(), text("text"))
        }
        assertThrows<QueryException> {
            // after operator needs parameter
            callParser(after())
        }
        assertThrows<QueryException> {
            // wrong date format
            callParser(after(), text("27-5-2023"))
        }
    }

    @Test
    fun testBefore() {
        assertEquals(
            "BEFORE('startDate','2023-05-27')",
            callParser(
                text("startDate"),
                before(),
                text("2023-05-27"),
            ),
        )
    }

    @Test
    fun testAfter() {
        assertEquals(
            "AFTER('startDate','2023-05-27')",
            callParser(
                text("startDate"),
                after(),
                text("2023-05-27"),
            ),
        )
    }

    @Test
    fun testDurationShorter() {
        assertEquals(
            "DURATIONSHORTER('startDate','endDate',-2)",
            callParser(
                duration(),
                text("startDate"),
                text("endDate"),
                shorter(),
                number("-2"),
            ),
        )
    }

    @Test
    fun testDurationLonger() {
        assertEquals(
            "DURATIONLONGER('startDate','endDate',2.6)",
            callParser(
                duration(),
                text("startDate"),
                text("endDate"),
                longer(),
                number("2.6"),
            ),
        )
    }

    @Test
    fun testOperatorPrecedence() {
        assertEquals(
            "OR(AND(NOT(CONTAINS('field','text')),CONTAINS('field','text')),CONTAINS('field','text'))",
            callParser(
                not(),
                text("field"),
                contains(),
                text("text"),
                and(),
                text("field"),
                contains(),
                text("text"),
                or(),
                text("field"),
                contains(),
                text("text"),
            ),
        )
    }

    @Test
    fun testHasField() {
        assertEquals(
            "HASFIELD('field')",
            callParser(
                hasField(),
                text("field"),
            ),
        )
    }

    @Test
    fun testIsInNextSeconds() {
        assertEquals(
            "ISINNEXTSECONDS('field',3600)",
            callParser(
                text("field"),
                isInNextSeconds(),
                number("3600"),
            ),
        )
    }

    @Test
    fun testIsInLastSeconds() {
        assertEquals(
            "ISINLASTSECONDS('field',3600)",
            callParser(
                text("field"),
                isInLastSeconds(),
                number("3600"),
            ),
        )
    }

    private fun before(): Token = Token(TokenType.BEFORE, null)

    private fun after(): Token = Token(TokenType.AFTER, null)

    private fun grOpen(): Token = Token(TokenType.GROUPING_OPEN, null)

    private fun grClose(): Token = Token(TokenType.GROUPING_CLOSE, null)

    private fun not(): Token = Token(TokenType.NOT, null)

    private fun or(): Token = Token(TokenType.OR, null)

    private fun and(): Token = Token(TokenType.AND, null)

    private fun equals(): Token = Token(TokenType.EQUALS, null)

    private fun contains(): Token = Token(TokenType.CONTAINS, null)

    private fun text(s: String): Token = Token(TokenType.TEXT, s)

    private fun number(number: String): Token = Token(TokenType.NUMBER, null, BigDecimal(number))

    private fun duration(): Token = Token(TokenType.DURATION, null)

    private fun shorter(): Token = Token(TokenType.SHORTER, null)

    private fun longer(): Token = Token(TokenType.LONGER, null)

    private fun hasField(): Token = Token(TokenType.HAS_FIELD, null)

    private fun isInNextSeconds(): Token = Token(TokenType.IS_IN_NEXT_SECONDS, null)

    private fun isInLastSeconds(): Token = Token(TokenType.IS_IN_LAST_SECONDS, null)

    private fun callParser(vararg tokens: Token): String = Parser(tokens.toList()).parse().toString()
}
