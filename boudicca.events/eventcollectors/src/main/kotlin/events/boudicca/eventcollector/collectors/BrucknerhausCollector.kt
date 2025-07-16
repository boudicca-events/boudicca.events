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
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class BrucknerhausCollector : TwoStepEventCollector<Element>("brucknerhaus") {

    override fun getAllUnparsedEvents(): List<Element> {
        val events = mutableListOf<Element>()

        val fetcher = FetcherFactory.newFetcher()
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.brucknerhaus.at/programm/veranstaltungen"))
        val otherUrls = document.select("ul.pagination>li")
            .toList()
            .filter { it.attr("class").isEmpty() }
            .map { "https://www.brucknerhaus.at" + it.children().first()!!.attr("href") }

        events.addAll(findUnparsedEvents(document))

        otherUrls.forEach {
            events.addAll(findUnparsedEvents(Jsoup.parse(fetcher.fetchUrl(it))))
        }

        return events
    }

    private fun findUnparsedEvents(doc: Document): List<Element> {
        return doc.select("div.event div.event__element")
    }

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent> {
        val startDates = parseDate(event)
        val name = event.select("div.event__name").text()
        val url = "https://www.brucknerhaus.at" + event.select("a.headline_link").attr("href")

        var description = event.select("div.event__teaser p").text()
        if (description.isBlank()) {
            description = event.select("div.event__teaser .fr-view").first()?.children()?.first()?.text() ?: ""
        }

        return structuredEvent(name, startDates) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                UrlUtils.parse(event.select("div.event__image img").attr("src"))
            )
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.TYPE_PROPERTY, "concert") //TODO check
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Brucknerhaus") //TODO not all events are there...
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.brucknerhaus.at/"))
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
