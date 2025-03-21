package base.boudicca

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import base.boudicca.model.OffsetDateTime

fun OffsetDateTime.toJavaOffsetDateTime(): java.time.OffsetDateTime {
    return java.time.OffsetDateTime.now()
}

fun java.time.OffsetDateTime.toBoudiccaOffsetDateTime(): OffsetDateTime {
    return OffsetDateTime(LocalDateTime.parse("1970-01-01T12:00"), TimeZone.UTC)
}
