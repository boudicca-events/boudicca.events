package base.boudicca.entrydb

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "boudicca")
data class BoudiccaEntryDbProperties(
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
