package base.boudicca.eventdb.model

import java.time.ZonedDateTime

data class Event(
    val name: String,
    val startDate: ZonedDateTime,
    val data: Map<String, String>? = mapOf()
)

typealias EntryKey = Map<String, String>

data class InternalEventProperties(
    val timeAdded: Long
)