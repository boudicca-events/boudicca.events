package base.boudicca.format

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import base.boudicca.model.OffsetDateTime

class DateFormatAdapterTest {
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
            OffsetDateTime(LocalDateTime(2024, 10, 21, 11, 12, 34, 0), UtcOffset(2).asTimeZone()),
            parseFromString("2024-10-21T11:12:34+02:00")
        )
    }

    private fun parseFromString(s: String): OffsetDateTime {
        return DateFormatAdapter().fromString(s)
    }

    @Test
    fun testParseToStringValidValues() {
        assertEquals(
            "2024-10-21T11:12:34+02:00",
            parseToString(
                OffsetDateTime(LocalDateTime(2024, 10, 21, 11, 12, 34, 0), UtcOffset(2).asTimeZone())
            )
        )
    }

    private fun parseToString(date: OffsetDateTime): String {
        return DateFormatAdapter().convertToString(date)
    }
}
