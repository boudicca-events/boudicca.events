package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.dateparser.dateparser.reduce
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class StadthalleWienCollector : TwoStepEventCollector<String>("stadthallewien") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.stadthalle.com"

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.stadthalle.com/de/events/alle-events"))
        return document.select("div.event-item-inner a.front-side").map { baseUrl + it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))
        val name = eventSite.select("h1.title").text()

        val imgTag = eventSite.select("div.img-ad img")
        var srcAttr = imgTag.attr("src")
        if (srcAttr.isBlank()) {
            val bgImgStyle = eventSite.select(".bg-img").attr("style")
            srcAttr = if (bgImgStyle.contains("url(")) {
                bgImgStyle.split("url(")[1].split(")")[0]
            } else {
                eventSite.select("h1#logo img").attr("src")
            }
        }
        val pictureUrl = UrlUtils.parse(baseUrl, srcAttr)

        val altText = imgTag.attr("alt")
        var copyright = "Stadthalle Wien"
        if (altText.contains("©")) {
            copyright = altText.split("©")[1].trim()
        }

        val startDates = parseDates(eventSite) // we could parse enddates but this is kinda tricky for some

        return startDates.dates.map { date ->
            structuredEvent(name, date.startDate) {
                withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
                withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
                withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, copyright)
                withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, altText)
                withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.readmore-txt").text())
                withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Stadthalle Wien")
                withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
                withProperty(SemanticKeys.ENDDATE_PROPERTY, date.endDate)
            }
        }
    }

    private fun parseDates(eventSite: Element): DateParserResult {
        return try {
            eventSite.select("ul#datetable li").map {
                DateParser.parse(it.text())
            }.reduce()
        } catch (_: IllegalArgumentException) {
            // sometimes the timetable is empty, try fallback to header
            DateParser.parse(eventSite.select("div.description h3.date").text())
        }
    }
}
