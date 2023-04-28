package events.boudicca.api.eventcollector

import java.time.OffsetDateTime

data class Event(
    val name: String,
    val startDate: OffsetDateTime,
    val additionalData: Map<String, String>
)
