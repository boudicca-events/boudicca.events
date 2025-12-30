package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["chelsea"])
class ChelseaCollector : TwoStepEventCollector<Element>("chelsea") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.chelsea.co.at/"

    override fun getAllUnparsedEvents(): List<Element> =
        Jsoup
            .parse(fetcher.fetchUrl(baseUrl + "concerts.php"))
            .select("table.termindetails")

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent?> {
        val name = event.select("div.band").text()
        val bandNames = name.split("/").map { it.trim() }
        val startDate = DateParser.parse(event.select("div.date").text())
        val description = event.select("div.text").text()
        val imageSource = event.select("img").first()?.attr("src")

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(baseUrl, imageSource))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Chelsea")
            withProperty(SemanticKeys.CONCERT_BANDLIST_PROPERTY, bandNames)
            withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.MUSIC)
            withProperty(SemanticKeys.TYPE_PROPERTY, "concert")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Chelsea")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
        }
    }
}
