package base.boudicca;

import java.time.OffsetDateTime

data class Event(
    val name: String,
    val startDate: OffsetDateTime,
    val data: Map<String, String> = mapOf()
)