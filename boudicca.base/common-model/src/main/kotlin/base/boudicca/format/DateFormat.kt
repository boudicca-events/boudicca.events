package base.boudicca.format

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * parsing utils to get string values to format date and back
 *
 * date format is (for now, lets see if we extend it) the DateTimeFormatter.ISO_DATE_TIME format
 *
 * all methods may throw exceptions on wrong formatted values
 */
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
