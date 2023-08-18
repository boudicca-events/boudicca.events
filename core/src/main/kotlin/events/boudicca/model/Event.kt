package events.boudicca.model

import java.time.ZonedDateTime

data class Event(
        val name: String,
        val startDate: ZonedDateTime,
        val data: Map<String, String>? = mapOf()
)

data class EventKey(
        val name: String,
        val startDate: ZonedDateTime,
)