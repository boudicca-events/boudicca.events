package base.boudicca.api.remotecollector.model

import java.time.OffsetDateTime

data class HttpCall(
    val url: String,
    val responseCode: Int,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
    val postParams: String?
)
