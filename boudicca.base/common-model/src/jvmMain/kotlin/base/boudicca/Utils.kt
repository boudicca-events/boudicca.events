package base.boudicca

import base.boudicca.model.OffsetDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

fun OffsetDateTime.toJava(): java.time.OffsetDateTime {
    return java.time.OffsetDateTime.now()
}

fun java.time.OffsetDateTime.toBoudiccaOffsetDateTime(): OffsetDateTime {
    return OffsetDateTime(LocalDateTime.parse("1970-01-01T12:00"), TimeZone.UTC)
}
