package base.boudicca.format

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


actual typealias Date = OffsetDateTime

actual object DateParser {
    actual fun parseDate(date: String): Date {
        return OffsetDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
    }

    actual fun dateToString(date: Date): String {
        return DateTimeFormatter.ISO_DATE_TIME.format(date)
    }
}
