package events.boudicca.search.model

import java.time.ZonedDateTime

data class Event(
        val name: String,
        val startDate: ZonedDateTime,
        val data: Map<String, String>? = mapOf()
)