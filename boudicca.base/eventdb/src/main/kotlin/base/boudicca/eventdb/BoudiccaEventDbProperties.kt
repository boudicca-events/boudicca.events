package base.boudicca.eventdb

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "boudicca")
data class BoudiccaEventDbProperties(
    val store: Store,
    val ingest: Ingest,
    val entryKeyNames: List<String>?,
)

data class Store(
    val path: String?
)

data class Ingest(
    val password: String?
)