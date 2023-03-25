package at.cnoize.boudicca.api

import at.cnoize.boudicca.api.util.KOffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class SearchDTO(
        val name: String?,
        @Serializable(KOffsetDateTimeSerializer::class) val fromDate: OffsetDateTime?,
        @Serializable(KOffsetDateTimeSerializer::class) val toDate: OffsetDateTime?,
)

