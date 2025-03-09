package base.boudicca.publisher.event.html

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "boudicca")
data class PublisherHtmlProperties(
    val devMode: Boolean = false,
    val headerTitle: String = "BOUDICCA.EVENTS",
    val pageTitle: String = "Boudicca.Events - find accessible events in Austria",
)
