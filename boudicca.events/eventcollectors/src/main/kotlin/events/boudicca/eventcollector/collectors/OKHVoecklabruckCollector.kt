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

class OKHVoecklabruckCollector : TwoStepEventCollector<Pair<String, String>>("okhvoecklabruck") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.okh.or.at/"

    override fun getAllUnparsedEvents(): List<Pair<String, String>> {
        val events = mutableListOf<Pair<String, String>>()
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "programm"))
        document.select("div.event_box").forEach {
            events.add(
                Pair(
                    it.select("a.event-inner").attr("href"),
                    it.select("div.event-art").text()
                )
            )
        }
        return events
    }

    override fun parseMultipleStructuredEvents(event: Pair<String, String>): List<StructuredEvent?>? {
        val (eventUrl, eventType) = event
        val url = baseUrl + eventUrl
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1").text()
        val dates = parseDates(eventSite)

        val locationInfo = eventSite.select("p.ort").text()
        val accessibleEntry = locationInfo.contains("Barrierefreier Zugang")

        return structuredEvent(name, dates) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(SemanticKeys.TYPE_PROPERTY, eventType)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.box_3").text())
            withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                UrlUtils.parse(baseUrl + eventSite.select("div#headerpic img.header").attr("src").trim())
            )
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Offenes Kulturhaus Vöcklabruck")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Vöcklabruck")
            withProperty(SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY_PROPERTY, accessibleEntry)
        }
    }

    private fun parseDates(element: Element): DateParserResult {
        val fullDateTime = element.select("div.box_datum p")

        val dateText = fullDateTime[0].text()
        return if (fullDateTime.size > 1 && fullDateTime[1].textNodes().isNotEmpty()) {
            DateParser.parse(dateText, fullDateTime[1].textNodes().first().text())
        } else {
            DateParser.parse(dateText)
        }
    }

}
