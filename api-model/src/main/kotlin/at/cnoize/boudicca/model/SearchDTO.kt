package at.cnoize.boudicca.model

import java.time.OffsetDateTime

data class SearchDTO(
    val name: String?,
    val fromDate: OffsetDateTime?,
    val toDate: OffsetDateTime?,
)

