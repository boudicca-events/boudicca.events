package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.annotations.BoudiccaEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@BoudiccaEventCollector("brucknerhaus")
class BrucknerhausCollector : TwoStepEventCollector<Element>("brucknerhaus") {
    private val baseUrl = "https://www.brucknerhaus.at"

    override fun getAllUnparsedEvents(): List<Element> {
        val events = mutableListOf<Element>()

        val fetcher = FetcherFactory.newFetcher()
        val document = Jsoup.parse(fetcher.fetchUrl("$baseUrl/programm/veranstaltungen"))
        val otherUrls =
            document
                .select("ul.pagination>li")
                .toList()
                .filter { it.attr("class").isEmpty() }
                .map { baseUrl + it.children().first()!!.attr("href") }

        events.addAll(findUnparsedEvents(document))

        otherUrls.forEach {
            events.addAll(findUnparsedEvents(Jsoup.parse(fetcher.fetchUrl(it))))
        }

        return events
    }

    private fun findUnparsedEvents(doc: Document): List<Element> = doc.select("div.event div.event__element")

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent> {
        val startDates = parseDate(event)
        val name = event.select("div.event__name").text()
        val url = baseUrl + event.select("a.headline_link").attr("href")

        var description = event.select("div.event__teaser p").text()
        if (description.isBlank()) {
            description = event
                .select("div.event__teaser .fr-view")
                .first()
                ?.children()
                ?.first()
                ?.text() ?: ""
        }

        val imgTag = event.select("div.event__image img")
        val imgAltText = imgTag.attr("alt")
        var imgCopyright = "Brucknerhaus Linz"
        if (imgAltText.contains("©")) {
            imgCopyright = imgAltText.split("©")[1].trim()
        }

        var location = event.select(".event__location").text()
        location =
            if (location.contains("Brucknerhaus")) {
                "Brucknerhaus Linz"
            } else {
                // clean up location that looks like "18:00 Haupteingang Anton Bruckner Privatuniversität Linz"
                location.substring(5).replace("Haupteingang", "").trim()
            }

        return structuredEvent(name, startDates) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgTag.attr("src")))
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, imgAltText)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, imgCopyright)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.TYPE_PROPERTY, "concert") // TODO check
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, location)
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
        }
    }

    private fun parseDate(event: Element): DateParserResult {
        val dateElement = event.select("div.event__date").first()!!
        val timeElement = event.select("div.event__location").first()!!
        val date = dateElement.text()
        val time = timeElement.children()[0].children()[0].text()
        return DateParser.parse(date, time)
    }
}
