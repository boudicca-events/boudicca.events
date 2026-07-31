package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.model.structured.StructuredEvent
import events.boudicca.eventcollector.util.fetchUrlAndParse
import events.boudicca.eventcollector.util.withDescription

class OOESeniorenbundCollector : TwoStepEventCollector<String>("ooesb") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://ooesb.at/veranstaltungen/"

    override fun getAllUnparsedEvents(): List<String> {
        val document = fetcher.fetchUrlAndParse(baseUrl)

        println(document)

        return document
            .select("div.eventmain div.eventimage-container a")
            .map {
                println("WTF MAN: " + it)
                it.attr("abs:href")
            }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent> {
        val eventDoc = fetcher.fetchUrlAndParse(event)

        val name = eventDoc.select("div.eventmain h2").first()!!.text()
        val dates = DateParser.parse(eventDoc.select("div.eventmain table tr td").first()!!.text())
        // TODO location name and city here are not seperated at all -.-
        val location = eventDoc.select("div.eventmain table tr td").last()!!.text()
        val description =
            eventDoc
                .select("div.eventmain>div>div")
                .dropWhile { it.tagName() != "table" }
                .drop(1)
                .takeWhile { it.tagName() != "a" }

        return structuredEvent(name, dates) {
            withProperty(SemanticKeys.URL_PROPERTY, event)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, location)
            withDescription(description)
            withProperty(SemanticKeys.SOURCES_PROPERTY, event)
        }
    }
}
