package base.boudicca.format

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.jvm.Throws

object DateFormat {
    @Throws(IllegalArgumentException::class)
    fun parseFromString(value: String): OffsetDateTime {
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("could not parse string value $value into date", e)
        }
    }

    fun parseToString(date: OffsetDateTime): String {
        return DateTimeFormatter.ISO_DATE_TIME.format(date)
    }
}