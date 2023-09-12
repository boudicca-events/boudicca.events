package events.boudicca.search.model

import java.time.OffsetDateTime

data class SearchDTO(
    val name: String? = null,
    val fromDate: OffsetDateTime? = null,
    val toDate: OffsetDateTime? =null,
    val category: String? = null,
    val locationName: String? = null,
    val locationCity: String? = null,
    val offset: Int? = null,
    val size: Int? = null,
    val flags: List<String?>? = null,
    val durationShorter: Double? = null,
    val durationLonger: Double? = null,
)