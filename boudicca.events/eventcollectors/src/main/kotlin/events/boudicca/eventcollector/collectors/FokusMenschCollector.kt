package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.model.structured.StructuredEvent

class FokusMenschCollector : IcalCollector("fokusmensch") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://fokusmensch.info"
    private val icsUrl = "$baseUrl/veranstaltungen.ics"

    override fun getAllIcalResources(): List<String> = listOf(fetcher.fetchUrl(icsUrl))

    override fun postProcess(event: StructuredEvent): StructuredEvent {
        val builder =
            event
                .toBuilder()
                .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(icsUrl, baseUrl))
                .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, "https://fokusmensch.info/files/fm-theme/layout/OOEZIV_Logo_FokusMensch_72dpi.png")

        return builder.build()
    }
}
