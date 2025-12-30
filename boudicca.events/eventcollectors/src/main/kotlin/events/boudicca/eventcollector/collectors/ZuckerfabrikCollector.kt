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
import org.jsoup.select.Elements
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["zuckerfabrik"])
class ZuckerfabrikCollector : TwoStepEventCollector<String>("zuckerfabrik") {
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.zuckerfabrik.at/termine-tickets/"))
        return document.select("div#storycontent > a.bookmarklink").map { it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        var name = eventSite.select("div#storycontent>h2").text()

        val storycontent = eventSite.select("div#storycontent>p")
        if (storycontent[0].text().isNotBlank()) {
            name += " - " + storycontent[0].text()
        }
        val dateIndex = findDateIndex(storycontent)
        val (dates, type) = parseTypeAndDate(storycontent[dateIndex])
        val description = ((dateIndex + 1) until storycontent.size).joinToString("\n") { storycontent[it].text() }

        val figure = eventSite.select("div#storycontent figure")
        var pictureCopyright = figure.selectFirst("figcaption")?.text() ?: "Zuckerfabrik"
        pictureCopyright = pictureCopyright.replace("(c)", "").trim()
        var pictureUrl = figure.selectFirst("img")?.attr("src")
        if (pictureUrl.isNullOrBlank()) {
            pictureUrl = eventSite.selectFirst("div#storycontent img")?.attr("src")
        }

        return structuredEvent(name, dates) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, pictureCopyright)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Zuckerfabrik")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.zuckerfabrik.at"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Enns")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
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
