package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.annotations.BoudiccaEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup

@BoudiccaEventCollector("burgclam")
class BurgClamCollector : TwoStepEventCollector<String>("burgclam") {
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://clamlive.at/shows/#/"))
        return document
            .select("section.eventCollection a")
            .map { it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("h1.eventTitle").text()
        val dateText = eventSite.select("div.eventDate").text()
        val startDate = DateParser.parse(dateText)

        var description = eventSite.select("section.eventSingle__description").text()
        val lineupElement = eventSite.select("li.lineupList__item")
        if (lineupElement.isNotEmpty()) {
            val lineup = "Line-up:\n" + lineupElement.map { it.text() }.joinToString("\n") + "\n"
            description = lineup + description
        }

        val imgTag = eventSite.select(".eventSingle__headerBanner noscript img")
        val imgAltText = imgTag.attr("alt")

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgTag.attr("src")))
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, imgAltText)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Burg Clam")
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Burg Clam")
            withProperty(SemanticKeys.TYPE_PROPERTY, "concert")
        }
    }
}
