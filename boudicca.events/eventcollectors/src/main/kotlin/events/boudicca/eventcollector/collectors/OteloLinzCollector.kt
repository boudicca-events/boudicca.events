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

class OteloLinzCollector : TwoStepEventCollector<String>("otelolinz") {

    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.otelolinz.at/veranstaltungen/"))
        return document.select("table.events-table tr a")
            .map { it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("div.article-inner h1").text()

        val img = eventSite.select("div.entry-content img")
        val pictureUrl = if (!img.isEmpty()) {
            UrlUtils.parse(img.first()!!.attr("src"))
        } else {
            null
        }

        return structuredEvent(name, parseDates(eventSite)) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.TYPE_PROPERTY, "technology")
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, getDescription(eventSite))
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(
                SemanticKeys.LOCATION_NAME_PROPERTY,
                eventSite.select("div#em-event-6>p")[1].select("a").text()
            )
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
        }
    }

    private fun getDescription(eventSite: Document): String {
        val sb = StringBuilder()

        var foundBreak = false
        for (e in eventSite.select("div#em-event-6").first()!!.children()) {
            if (e.tagName() == "br") {
                foundBreak = true
            } else {
                if (foundBreak) {
                    val text = e.text()
                    if (text.isNotBlank()) {
                        sb.append(text)
                        sb.appendLine()
                    }
                }
            }
        }

        return sb.toString()
    }

    private fun parseDates(element: Element): DateParserResult {
        val dateText = element.select("div#em-event-6>p").first()!!.textNodes()[1].text().trim()
        val timeText = element.select("div#em-event-6>p").first()!!.select("i").text()

        return DateParser.parse(dateText, timeText)
    }

}
