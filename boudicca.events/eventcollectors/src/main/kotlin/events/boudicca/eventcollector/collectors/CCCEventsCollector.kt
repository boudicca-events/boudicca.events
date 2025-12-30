package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Events from the CCC Event Blog
 */
@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["events_ccc_de"])
class CCCEventsCollector : IcalCollector("events.ccc.de") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://events.ccc.de/"
    private val icsUrl = "${baseUrl}calendar/events.ics"

    override fun getAllIcalResources(): List<String> = listOf(fetcher.fetchUrl(icsUrl))

    override fun postProcess(event: StructuredEvent): StructuredEvent =
        event
            .toBuilder()
            .withProperty(
                SemanticKeys.TAGS_PROPERTY,
                listOf("Chaos", "CCC", "tech", "privacy", "hacking", "making", "programming"),
            ).withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.TECH)
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl, icsUrl))
            .withProperty(SemanticKeys.TYPE_PROPERTY, "chaosevent")
            .build()
}
