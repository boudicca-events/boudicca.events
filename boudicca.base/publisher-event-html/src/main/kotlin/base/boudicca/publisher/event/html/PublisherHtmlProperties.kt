package base.boudicca.publisher.event.html

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "boudicca")
data class PublisherHtmlProperties(
    val localMode: Boolean = false,
)
