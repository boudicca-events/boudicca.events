package at.cnoize.boudicca

import java.time.Instant

data class SearchDTO(
        var name: String?,
        var fromDate: Instant?,
        var toDate: Instant?,
)
