package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class KapuCollector : TwoStepEventCollector<String>("kapu") {

    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.kapu.or.at/events"))
        return document.select("article.event")
            .map { it.attr("about") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val url = "https://www.kapu.or.at$event"
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1").text()
        val startDate = parseDate(eventSite)

        var description = eventSite.select("div.textbereich__field-text").text()
        if (description.isBlank()) {
            description = eventSite.select("div.text-bild__field-image-text").text()
        }

        val imgSrc = eventSite.select("article.event img.media__element").attr("data-src")
        val pictureUrl = if (imgSrc.isNotBlank()) {
            "https://www.kapu.or.at$imgSrc"
        } else {
            null
        }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(
                SemanticKeys.TYPE_PROPERTY,
                eventSite.select("article.event > div.container > div.wot").text()
            )
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                if (pictureUrl != null) UrlUtils.parse(pictureUrl) else null
            )
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Kapu")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.kapu.or.at"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
        }
    }

    private fun parseDate(element: Element): DateParserResult {
        val fullDateTime = element.select("article.event > div.container div.wob:nth-child(1)").text()

        return DateParser.parse(fullDateTime)
    }

}
