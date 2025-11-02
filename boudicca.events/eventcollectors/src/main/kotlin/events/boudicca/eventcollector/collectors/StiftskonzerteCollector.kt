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

class StiftskonzerteCollector : TwoStepEventCollector<String>("stiftskonzerte") {

    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.stiftskonzerte.at/programm-und-karten/"))
        return document.select("div.entry-footer-links a").not("a.open-modal")
            .map { it.attr("href") }
            .filter {
                //this is not an event but some ticket information thingy
                it != "https://www.stiftskonzerte.at/programm-und-karten/mitglieder-vvk-ooe-stiftskonzerte-2024/"
            }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))
        val name = eventSite.select("header.entry-header h1").text()

        val locationTimeDiv = eventSite.select("div.entry-content div.location")
        locationTimeDiv.select("br").after("\\n")
        val locationAndTime = locationTimeDiv.text()
            .split("\\n")
            .map { it.trim().replace("ü", "ü") }  // fix different ü's in Kremsmünster
            .filter { it.isNotBlank() }
        val startDate = parseDate(eventSite, locationAndTime)

        val city = locationAndTime[1].replace("Stift ", "")
        val location = locationAndTime.subList(1, locationAndTime.lastIndex + 1).joinToString(", ")

        val img = eventSite.select("div.entry-content img")
        val pictureUrl = UrlUtils.parse(img.attr("src"))

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.entry-flexible-content").text())
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, city)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, location)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
        }
    }

    private fun parseDate(element: Element, locationAndTime: List<String>): DateParserResult {
        val fullDate = element.select("div.entry-content div.date").text()
        return DateParser.parse(fullDate, locationAndTime[0])
    }

}
