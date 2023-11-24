package base.boudicca.search

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "boudicca")
data class BoudiccaSearchProperties(
    val localMode: Boolean,
    val eventDB: EventDBProperties,
)

data class EventDBProperties(
    val url: String
)
