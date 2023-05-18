package events.boudicca.model

import java.time.OffsetDateTime

data class FilterDTO(
        val name: String?,
        val fromDate: OffsetDateTime?,
        val toDate: OffsetDateTime?,
)