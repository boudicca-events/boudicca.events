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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["kunstunilinz"])
class KunstuniversitaetLinzCollector : TwoStepEventCollector<String>("kunstunilinz") {
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        var document = Jsoup.parse(fetcher.fetchUrl("https://events.kunstuni-linz.at/"))

        val eventLinks = mutableListOf<String>()
        while (true) {
            eventLinks.addAll(document.select("h3.tribe-events-calendar-list__event-title a").map { it.attr("href") })
            val nextPageButtons = document.select("a.tribe-events-c-nav__next").map { it.attr("href") }
            if (nextPageButtons.isEmpty()) {
                break
            }
            document = Jsoup.parse(fetcher.fetchUrl(nextPageButtons.first()))
        }
        return eventLinks
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val url = event
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1.tribe-events-single-event-title").text()
        val startDate = parseDate(eventSite)

        val description = eventSite.select("div.tribe-events-single-event-description").text()

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
            withProperty(SemanticKeys.TAGS_PROPERTY, tags)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgSrc))
            withProperty(
                SemanticKeys.LOCATION_NAME_PROPERTY,
                if (location.isBlank()) "Kunstuniversität Linz" else location,
            )
            withProperty(
                SemanticKeys.LOCATION_URL_PROPERTY,
                UrlUtils.parse(locationUrl.ifBlank { "https://www.kunstuni-linz.at/" }),
            )
            withProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY, locationAddress)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Kunstuniversität Linz")
        }
    }

    private fun parseDate(element: Element): DateParserResult {
        val fullDateTime = element.select("div.tribe-events-schedule").text()

        return DateParser.parse(fullDateTime)
    }
}
