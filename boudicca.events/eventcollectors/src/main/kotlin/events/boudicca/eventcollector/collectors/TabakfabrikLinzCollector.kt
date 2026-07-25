package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import events.boudicca.eventcollector.util.fetchUrlAndParse
import events.boudicca.eventcollector.util.withDescription
import org.jsoup.nodes.Document

class TabakfabrikLinzCollector : TwoStepEventCollector<String>("tabakfabriklinz") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://tabakfabrik-linz.at/"
    private val locationName = "Tabakfabrik Linz"

    override fun getAllUnparsedEvents(): List<String> =
        fetcher
            .fetchUrlAndParse(baseUrl + "events")
            .select("#event-posts-wrapper a.event-link-overlay")
            .mapNotNull { it.attr("href") }
            .distinct()

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val document = fetcher.fetchUrlAndParse(event)
        val name = document.select("h1.wp-block-heading").text()
        val description = document.select("article.event > div")

        val date =
            DateParser.parse(
                document.select("article.event section.post-type-event p.event-date-from .date-inner").text(),
                document.select("article.event section.post-type-event p.location-time-wrapper .time").text(),
            )

        var location = document.select("article.event section.post-type-event p.location-time-wrapper .location").text()
        if (location.isBlank()) {
            location = locationName
        } else if (!location.startsWith("Tabakfabrik")) {
            location = "$locationName: $location" // room names make more sense with the prefix
        }

        val imageDiv = document.select("article.event section.post-type-event div.page-header-img-container")
        val imgSrc =
            if (imageDiv.hasAttr("data-bg")) {
                imageDiv.attr("data-bg")
            } else {
                val headerStyle = imageDiv.attr("style")
                if (headerStyle.contains("background-image: url")) {
                    headerStyle.split("background-image: url(")[1].split(")")[0]
                } else {
                    ""
                }
            }.removePrefix("\"").removeSuffix("\"")

        return structuredEvent(name, date) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withDescription(description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgSrc))
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY, location)
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, locationName)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, locationName)
        }
    }

    private fun parseDate(
        document: Document,
        propName: String,
    ): DateParserResult? {
        val dateElements = document.select("[itemprop='$propName']")
        if (dateElements.isNotEmpty()) {
            return DateParser.parse(dateElements.first()!!.attr("content"))
        }
        return null
    }
}
