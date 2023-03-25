package at.cnoize.boudicca

import java.time.Instant

data class Event(
    val name: String,
    val startDate: Instant,
    val data: Map<String, String>? = mapOf()
)
