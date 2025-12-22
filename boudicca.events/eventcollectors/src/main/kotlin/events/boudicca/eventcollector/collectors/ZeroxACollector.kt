package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.model.EventCategory
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent

class ZeroxACollector : IcalCollector("ZeroxA") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://0xa.at/"
    private val icsUrl = "${baseUrl}events.ics"

    override fun getAllIcalResources(): List<String> = listOf(fetcher.fetchUrl(icsUrl))

    override fun postProcess(event: StructuredEvent): StructuredEvent = event
        .toBuilder()
        .withProperty(SemanticKeys.TAGS_PROPERTY, listOf("0xA", "Science", "Association", "JKU", "Universität", "Studieren"))
        .withProperty(SemanticKeys.TYPE_PROPERTY, "techmeetup") // TODO same as with Technologieplauscherl
        .withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.TECH)
        .withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.FREE)
        .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(icsUrl, baseUrl))
        .build()
}
