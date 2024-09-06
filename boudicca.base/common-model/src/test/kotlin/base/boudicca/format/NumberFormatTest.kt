package base.boudicca.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class NumberFormatTest {
    @Test
    fun testParseFromStringInvalidValues() {
        assertThrows<IllegalArgumentException> { parseFromString("") }
        assertThrows<IllegalArgumentException> { parseFromString("asd") }
        assertThrows<IllegalArgumentException> { parseFromString("+2-") }
    }

    @Test
    fun testParseFromStringValidValues() {
        assertEquals(BigDecimal.valueOf(123), parseFromString("123"))
        assertEquals(BigDecimal.valueOf(-123), parseFromString("-123"))
        assertEquals(BigDecimal.valueOf(1.3), parseFromString("1.3"))
    }

    private fun parseFromString(s: String): Number {
        return NumberFormat.parseFromString(s)
    }

    @Test
    fun testParseToStringValidValues() {
        assertEquals("123", parseToString(BigDecimal.valueOf(123)))
        assertEquals("-123", parseToString(BigDecimal.valueOf(-123)))
        assertEquals("1.23", parseToString(BigDecimal.valueOf(1.23)))
    }

    private fun parseToString(date: Number): String {
        return NumberFormat.parseToString(date)
    }
}