package base.boudicca.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DateFormatTest {
    @Test
    fun testParseFromStringInvalidValues() {
        assertThrows<IllegalArgumentException> { parseFromString("") }
        assertThrows<IllegalArgumentException> { parseFromString("asd") }
        assertThrows<IllegalArgumentException> { parseFromString("2024-04-27T23:59:00/02:00") }
        assertThrows<IllegalArgumentException> { parseFromString("2024#04-27T23:59:00+02:00") }
        assertThrows<IllegalArgumentException> { parseFromString("2024") }
    }

    @Test
    fun testParseFromStringValidValues() {
        assertEquals(
            OffsetDateTime.of(2024, 10, 21, 11, 12, 34, 0, ZoneOffset.ofHours(2)),
            parseFromString("2024-10-21T11:12:34+02:00")
        )
    }

    private fun parseFromString(s: String): OffsetDateTime {
        return DateFormat.parseFromString(s)
    }

    @Test
    fun testParseToStringValidValues() {
        assertEquals(
            parseFromString("2024-10-21T11:12:34+02:00"),
            OffsetDateTime.of(2024, 10, 21, 11, 12, 34, 0, ZoneOffset.ofHours(2))
        )
    }
}