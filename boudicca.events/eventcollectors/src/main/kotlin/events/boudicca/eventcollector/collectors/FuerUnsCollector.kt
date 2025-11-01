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
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class FuerUnsCollector : TwoStepEventCollector<String>("fueruns") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.fuer-uns.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val events = mutableListOf<Element>()

        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "aktuelle-veranstaltungen/veranstaltungskalender?page=1"))
        val otherUrls = document.select("nav.pagination ul li a")
            .toList()
            .map { it.attr("href") }
        parseEventList(document, events)

        otherUrls.forEach {
            parseEventList(Jsoup.parse(fetcher.fetchUrl(baseUrl + it)), events)
        }

        return events.map { it.attr("href") }
    }

    private fun parseEventList(document: Document, events: MutableList<Element>) {
        events.addAll(
            document.select("a.event.event_list_item.event_list_item_link")
                .toList()
                .filter { !it.attr("href").startsWith("http") } // exclude events from others than fuer uns
        )
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val fullEventLink = baseUrl + event
        val eventSite = Jsoup.parse(fetcher.fetchUrl(fullEventLink))

        val name = eventSite.select("h1").text()

        val startDate = parseDate(eventSite)

        val img = eventSite.select("div.event picture img")
        val pictureUrl = if (!img.isEmpty()) {
            UrlUtils.parse(baseUrl + img.first()!!.attr("src"))
        } else {
            null
        }
        val pictureAltText = img.first()?.attr("alt")

        val locationName = eventSite.select(".details_info .location").not("div.location.link-google-maps").text()

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(fullEventLink))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.field-text").text())
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, pictureAltText)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "füruns - Zentrum für Zivilgesellschaft ")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(fullEventLink))
            if (locationName.isNotEmpty()) {
                val regex = """(?<name>.*?)[,|\s]*(?<zip>\d{4}) (?<city>[\w\s]+)""".toRegex()
                val matchResult = regex.find(locationName)
                if (matchResult != null) {
                    withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, matchResult.groups["name"]!!.value.trimEnd())
                    withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, matchResult.groups["city"]!!.value.trimEnd())
                } else {
                    withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, locationName)
                }
            }
        }
    }

    private fun parseDate(element: Element): DateParserResult {
        val dtDiv = element.select("div.details_date_time")
        val date = dtDiv.select("div.date").text()
        val time = dtDiv.select("div.time").text()
        return DateParser.parse(date, time)
    }
}
