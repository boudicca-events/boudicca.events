package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DatePair
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["tabakfabriklinz"])
class TabakfabrikLinzCollector : TwoStepEventCollector<String>("tabakfabriklinz") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://tabakfabrik-linz.at/"
    private val locationName = "Tabakfabrik Linz"

    override fun getAllUnparsedEvents(): List<String> =
        Jsoup
            .parse(fetcher.fetchUrl(baseUrl + "events"))
            .select(".events-upcoming h1.entry-title a")
            .mapNotNull { it.attr("href") }
            .distinct()

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val document = Jsoup.parse(fetcher.fetchUrl(event))
        val name = document.select("h1.entry-title").text()
        val description = document.select("div.entry-content").text()

        var startDate = parseDate(document, "startDate")!!
        val endDate = parseDate(document, "endDate")
        if (endDate != null) {
            startDate = DateParserResult(listOf(DatePair(startDate.dates[0].startDate, endDate.dates[0].startDate)))
        }

        var location = document.select("[itemprop='location']").text()
        if (location.isBlank()) {
            location = locationName
        } else if (!location.startsWith("Tabakfabrik")) {
            location = "$locationName: $location" // room names make more sense with the prefix
        }

        val headerStyle = document.select("header.entry-header").first()!!.attr("style")
        var imgSrc = ""
        if (headerStyle.contains("background-image:url")) {
            imgSrc = headerStyle.split("background-image:url(")[1].split(")")[0]
        }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
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
