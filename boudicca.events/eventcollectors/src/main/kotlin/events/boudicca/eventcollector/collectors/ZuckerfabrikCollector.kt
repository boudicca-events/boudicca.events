package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import events.boudicca.eventcollector.util.fetchUrlAndParse
import events.boudicca.eventcollector.util.withDescription
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class ZuckerfabrikCollector : TwoStepEventCollector<String>("zuckerfabrik") {
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = fetcher.fetchUrlAndParse("https://www.zuckerfabrik.at/termine-tickets/")
        return document.select("div#storycontent > a.bookmarklink").map { it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val eventSite = fetcher.fetchUrlAndParse(event)

        var name = eventSite.select("div#storycontent>h2").text()

        val storycontent = eventSite.select("div#storycontent>p")
        if (storycontent[0].text().isNotBlank()) {
            name += " - " + storycontent[0].text()
        }
        val dateIndex = findDateIndex(storycontent)
        val (dates, type) = parseTypeAndDate(storycontent[dateIndex])

        val figure = eventSite.select("div#storycontent figure")
        var pictureCopyright = figure.selectFirst("figcaption")?.text() ?: "Zuckerfabrik"
        pictureCopyright = pictureCopyright.replace("(c)", "").trim()
        var pictureUrl = figure.selectFirst("img")?.attr("src")
        if (pictureUrl.isNullOrBlank()) {
            pictureUrl = eventSite.selectFirst("div#storycontent img")?.attr("src")
        }

        return structuredEvent(name, dates) {
            withDescription(storycontent.subList(dateIndex + 1, storycontent.size), false)
            withProperty(SemanticKeys.URL_PROPERTY, event)
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, pictureCopyright)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Zuckerfabrik")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, "https://www.zuckerfabrik.at")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Enns")
            withProperty(SemanticKeys.SOURCES_PROPERTY, event)
        }
    }

    private fun findDateIndex(storycontent: Elements): Int {
        for (i in 1 until storycontent.size) {
            if (storycontent[i].text().contains(" am ")) {
                return i
            }
        }
        error("could not find date index in: $storycontent")
    }

    private fun parseTypeAndDate(element: Element): Pair<DateParserResult, String> {
        val split = element.text().split(" am ")
        val type = split[0]
        val dates = DateParser.parse(split[1])
        return Pair(dates, type)
    }
}
