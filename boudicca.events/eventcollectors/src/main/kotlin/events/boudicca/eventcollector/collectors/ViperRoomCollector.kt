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

class ViperRoomCollector : TwoStepEventCollector<String>("viperroom") {
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val eventsList = Jsoup.parse(fetcher.fetchUrl("https://www.viper-room.at/veranstaltungen"))

        return eventsList.select("ul.events_list div.event_actions a:nth-child(1)")
            .map { it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("h1.entry-title").text()
        val startDate = parseDate(eventSite)

        val description = eventSite.select("div#em-event-6").first()!!
            .children()
            .toList()
            .filter {
                (it.tagName() == "div" &&
                        !(it.classNames().contains("event_price") || it.classNames().contains("event_actions")))
                        ||
                        (it.tagName() == "p" && !it.classNames().contains("event_time"))
            }
            .map { it.text() }
            .filter { it.isNotBlank() }
            .joinToString("\n")

        var img = eventSite.select("div#em-event-6 p img").first()
        img = img ?: eventSite.select("a.navbar-brand img").first() // logo
        val pictureUrl = UrlUtils.parse(img?.attr("src"))
        val pictureAltText = img?.attr("alt")

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.TYPE_PROPERTY, "concert")
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, pictureAltText)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Viper Room")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Viper Room")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
        }
    }

    private fun parseDate(event: Element): DateParserResult {
        val fullDateText = event.select("p.event_time").textNodes()[0].text()
        val fullTimeText = event.select("span.event_doors").text()
        return DateParser.parse(fullDateText, fullTimeText)
    }
}
