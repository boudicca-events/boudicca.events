package events.boudicca.api.search

import events.boudicca.api.search.BoudiccaQueryBuilder.after
import events.boudicca.api.search.BoudiccaQueryBuilder.and
import events.boudicca.api.search.BoudiccaQueryBuilder.before
import events.boudicca.api.search.BoudiccaQueryBuilder.contains
import events.boudicca.api.search.BoudiccaQueryBuilder.durationLonger
import events.boudicca.api.search.BoudiccaQueryBuilder.durationShorter
import events.boudicca.api.search.BoudiccaQueryBuilder.equals
import events.boudicca.api.search.BoudiccaQueryBuilder.escapeText
import events.boudicca.api.search.BoudiccaQueryBuilder.isQuery
import events.boudicca.api.search.BoudiccaQueryBuilder.not
import events.boudicca.api.search.BoudiccaQueryBuilder.or
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class BoudiccaQueryBuilderTest {

    @Test
    fun testEscapeText() {
        assertEquals("\"text\"", escapeText("text"))
        assertEquals("\"\\\"\"", escapeText("\""))
        assertEquals("\"\\\\\"", escapeText("\\"))
        assertEquals("\"text with spaces!\"", escapeText("text with spaces!"))
        assertEquals("\"text with \\\"quotes\\\"\"", escapeText("text with \"quotes\""))
        assertEquals("\"text with \\\\escapes\\\\\"", escapeText("text with \\escapes\\"))
        assertEquals("\"\"", escapeText(""))
    }

    @Test
    fun simpleContains() {
        val query = contains("field", "value")

        assertEquals("\"field\" contains \"value\"", query)
    }

    @Test
    fun simpleEquals() {
        val query = equals("field", "value")

        assertEquals("\"field\" equals \"value\"", query)
    }

    @Test
    fun simpleAnd() {
        val query = and(
            "first",
            "second"
        )

        assertEquals("(first) and (second)", query)
    }

    @Test
    fun singleAnd() {
        val query = and(
            "first"
        )

        assertEquals("(first)", query)
    }

    @Test
    fun multipleAnd() {
        val query = and(
            "first",
            "second",
            "third",
            "fourth"
        )

        assertEquals("(first) and (second) and (third) and (fourth)", query)
    }

    @Test
    fun simpleOr() {
        val query = or(
            "first",
            "second"
        )

        assertEquals("(first) or (second)", query)
    }

    @Test
    fun singleOr() {
        val query = or(
            "first"
        )

        assertEquals("(first)", query)
    }

    @Test
    fun multipleOr() {
        val query = or(
            "first",
            "second",
            "third",
            "fourth"
        )

        assertEquals("(first) or (second) or (third) or (fourth)", query)
    }

    @Test
    fun simpleNot() {
        val query = not("query")

        assertEquals("not (query)", query)
    }

    @Test
    fun simpleAfter() {
        val query = after(LocalDate.of(2023, 10, 6))

        assertEquals("after 2023-10-06", query)
    }

    @Test
    fun simpleBefore() {
        val query = before(LocalDate.of(2023, 10, 6))

        assertEquals("before 2023-10-06", query)
    }

    @Test
    fun simpleIs() {
        val query = isQuery(BoudiccaQueryBuilder.Category.ART)

        assertEquals("is ART", query)
    }

    @Test
    fun simpleDurationLonger() {
        val query = durationLonger(5.0)

        assertEquals("durationLonger 5.0", query)
    }

    @Test
    fun simpleDurationShorter() {
        val query = durationShorter(5.0)

        assertEquals("durationShorter 5.0", query)
    }

    @Test
    fun biggerNestedQuery() {
        val query = not(
            and(
                or(
                    contains("field", "value"),
                    contains("field2", "value2")
                ),
                equals("field3", "value3")
            )
        )

        assertEquals(
            """not ((("field" contains "value") or ("field2" contains "value2")) and ("field3" equals "value3"))""",
            query
        )
    }

    @Test
    fun testEmptyStringInputs() {
        assertThrows<IllegalArgumentException> { contains("", "") }
        assertDoesNotThrow { contains("field", "") }
        assertThrows<IllegalArgumentException> { equals("", "") }
        assertDoesNotThrow { equals("field", "") }

        assertThrows<IllegalArgumentException> { and("", "as") }
        assertThrows<IllegalArgumentException> { and("as", "") }
        assertThrows<IllegalArgumentException> { and("as", "as", "") }
        assertThrows<IllegalArgumentException> { or("", "as") }
        assertThrows<IllegalArgumentException> { or("as", "") }
        assertThrows<IllegalArgumentException> { or("as", "as", "") }

        assertThrows<IllegalArgumentException> { not("") }
    }

    @Test
    fun testPassNoSubQueries() {
        assertThrows<IllegalArgumentException> { and() }
        assertThrows<IllegalArgumentException> { or() }
    }

}