package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.model.EventCategory
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent

/**
 * VorAlpen Linux User Group
 */
class ValugCollector : IcalCollector("valug") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://valug.at/"
    private val icsUrl = "${baseUrl}events/index.ics"

    override fun getAllIcalResources(): List<String> {
        return listOf(fetcher.fetchUrl(icsUrl))
    }

    override fun postProcess(event: StructuredEvent): StructuredEvent {
        return event.toBuilder()
            .withProperty(SemanticKeys.TAGS_PROPERTY, listOf("VALUG", "Linux", "User Group"))
            .withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.TECH)
            .withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.FREE)
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl, icsUrl))
            .withProperty(SemanticKeys.TYPE_PROPERTY, "techmeetup") // TODO same as with Technologieplauscherl
            .build()
    }
}
