package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.model.structured.StructuredEvent
import events.boudicca.eventcollector.util.fetchUrlAndParse
import events.boudicca.eventcollector.util.withDescription
import org.jsoup.nodes.Element

class FraeuleinFlorentineCollector : TwoStepEventCollector<Element>("fraeuleinflorentine") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://frl-florentine.at/eventkalender/"

    override fun getAllUnparsedEvents(): List<Element> {
        val eventSite = fetcher.fetchUrlAndParse(baseUrl)

        return eventSite
            .select("div.gcal-day-block")
    }

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent?> {
        val nameAndTime = event.select(".gcal-event-title").text().split("|")
        val name = nameAndTime.first().trim()
        val startDate = DateParser.parse(listOf(event.select(".gcal-event-date").text()) + nameAndTime.drop(1))

        val description = event.select(".gcal-event-description")
        val url = event.select(".gcal-event-link").text()
        val imageUrl = event.select(".gcal-event-image img").attr("src")

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, url)
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withDescription(description)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Salonschiff Fräulein Florentine")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, "https://frl-florentine.at")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, imageUrl)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Salonschiff Fräulein Florentine")
        }
    }
}
