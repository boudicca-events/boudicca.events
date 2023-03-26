package at.cnoize.boudicca.api

import at.cnoize.boudicca.api.util.KZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.ZonedDateTime

@Serializable
data class Event(
    val name: String,
    @Serializable(KZonedDateTimeSerializer::class) val startDate: ZonedDateTime,
    val data: Map<String, String>? = mapOf()
)
