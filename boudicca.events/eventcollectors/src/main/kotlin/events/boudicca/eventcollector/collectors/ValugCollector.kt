package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.model.EventCategory
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * VorAlpen Linux User Group
 */
@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["valug"])
class ValugCollector : IcalCollector("valug") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://valug.at/"
    private val icsUrl = "${baseUrl}events/index.ics"

    override fun getAllIcalResources(): List<String> = listOf(fetcher.fetchUrl(icsUrl))

    override fun postProcess(event: StructuredEvent): StructuredEvent =
        event
            .toBuilder()
            .withProperty(SemanticKeys.TAGS_PROPERTY, listOf("VALUG", "Linux", "User Group"))
            .withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.TECH)
            .withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.FREE)
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl, icsUrl))
            .withProperty(SemanticKeys.TYPE_PROPERTY, "techmeetup") // TODO same as with Technologieplauscherl
            .build()
}
