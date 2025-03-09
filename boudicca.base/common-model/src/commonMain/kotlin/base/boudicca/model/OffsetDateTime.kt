package base.boudicca.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.format
import kotlinx.datetime.offsetIn
import kotlinx.datetime.toInstant

class OffsetDateTime(val localDateTime: LocalDateTime, val timeZone: TimeZone) {

    fun toIsoDateTimeString(): String =
        DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.format {
            setDateTime(localDateTime)
            setOffset(localDateTime.toInstant(timeZone).offsetIn(timeZone))
        }

    companion object {
        fun parseIsoDateTime(value: String): OffsetDateTime {
            val dateTimeOffset = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
                .parse(value)

            val zoneId = dateTimeOffset.timeZoneId
            val tz = zoneId?.let { TimeZone.of(zoneId) } ?: TimeZone.UTC
            return OffsetDateTime(dateTimeOffset.toLocalDateTime(), tz)
        }
    }
}
