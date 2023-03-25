package at.cnoize.boudicca.api

import at.cnoize.boudicca.api.util.KOffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class Event(val name: String, @Serializable(KOffsetDateTimeSerializer::class) val startDate: OffsetDateTime, val data: Map<String, String> = mapOf())