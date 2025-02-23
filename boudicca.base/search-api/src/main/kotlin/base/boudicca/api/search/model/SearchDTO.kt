package base.boudicca.api.search.model

import java.time.OffsetDateTime

data class SearchDTO(
    val name: String? = null,
    val fromDate: OffsetDateTime? = null,
    val toDate: OffsetDateTime? = null,
    val category: String? = null,
    val locationNames: List<String?>? = null,
    val locationCities: List<String?>? = null,
    val offset: Int? = null,
    val size: Int? = null,
    val flags: List<String?>? = null,
    val durationShorter: Double? = null,
    val durationLonger: Double? = null,
)
