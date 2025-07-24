package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class KunstuniversitaetLinzCollector : TwoStepEventCollector<String>("kunstunilinz") {

    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document =
            Jsoup.parse(fetcher.fetchUrl("https://events.kunstuni-linz.at/"))
        return document.select("div.tribe-events-calendar-list h3.tribe-events-calendar-list__event-title a")
            .map { it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val url = event
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1.tribe-events-single-event-title").text()
        val startDate = parseDate(eventSite)

        var description = eventSite.select("div.tribe-events-single-event-description").text()

        val imgSrc = eventSite.select("div.tribe-events-event-image img").attr("src")

        val type = eventSite.select("dd.tribe-events-event-categories a").firstOrNull()?.text()
        val tags = eventSite.select("dd.tribe-event-tags a").map { it.text() }
        val locationAddress = eventSite.select("dd.tribe-venue").text()
        val location = locationAddress.split(",").first()
        val locationUrl = eventSite.select("dd.tribe-venue a").attr("href")

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            if (tags.isNotEmpty()) withProperty(SemanticKeys.TAGS_PROPERTY, tags)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgSrc))
            withProperty(
                SemanticKeys.LOCATION_NAME_PROPERTY,
                if (location.isBlank()) "Kunstuniversität Linz" else location
            )
            withProperty(
                SemanticKeys.LOCATION_URL_PROPERTY,
                if (locationUrl.isBlank()) UrlUtils.parse("https://www.kunstuni-linz.at/") else UrlUtils.parse(
                    locationUrl
                )
            )
            withProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY, locationAddress)
        }
    }

    private fun parseDate(element: Element): DateParserResult {
        val fullDateTime = element.select("div.tribe-events-schedule").text()

        return DateParser.parse(fullDateTime)
    }

}
