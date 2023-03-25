package at.cnoize.boudicca

import java.util.*

data class Event(
    val name: String,
    val startDate: Date,
    val data: Map<String, String> = mapOf()
)
