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

class StadtwerkstattCollector : TwoStepEventCollector<String>("stadtwerkstatt") {
    private val baseUrl = "https://club.stwst.at/"
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))
        return document.select("div.single-event a")
            .map { it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        var name = eventSite.select("li.event-title").text()
        if (name.isBlank()) {
            name = eventSite.select("ul.event-artists span.name").joinToString(", ") { it.text().trim() }
        }
        val startDate = parseDate(eventSite)

        val type = eventSite.select("div.genre").text()
        val description = eventSite.select("div.event-text").text()

        val img = eventSite.select("div.event-text img")
        val logo = eventSite.selectFirst(".brand img")
        val pictureUrl = UrlUtils.parse(img.attr("src"))
            ?: UrlUtils.parse(logo?.attr("src"))

        if (name.isEmpty()) {
            name = description
        }

        //TODO could parse lineup

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Stadtwerkstatt")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Stadtwerkstatt")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
        }
    }

    private fun parseDate(element: Element): DateParserResult {
        val fullDate = element.select("div.date").text()
        val locationAndTime = element.select("div.location_time").text()
        return DateParser.parse(fullDate, locationAndTime)
    }
}
