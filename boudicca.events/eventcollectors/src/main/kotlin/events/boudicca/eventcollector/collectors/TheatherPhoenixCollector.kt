package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class TheatherPhoenixCollector : TwoStepEventCollector<Pair<Element, Element>>("theaterphoenix") {
    private val baseUrl = "https://www.theater-phoenix.at"
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<Pair<Element, Element>> {
        val document = Jsoup.parse(fetcher.fetchUrl("$baseUrl/termine"))
        return document.select("div.terminecol div.onecolum > div.terminlink")
            .map {
                val slug = it.attr("rel").removePrefix("/termine/")
                val details = document.select("div#termindetail div.termininfo[slug=$slug]").single()
                Pair(it, details)
            }
    }

    override fun parseMultipleStructuredEvents(event: Pair<Element, Element>): List<StructuredEvent?> {
        val (link, details) = event

        val name = link.select("div.terminetitel").text()

        val description = details.select("div.termindetailbeschreibung p").text()

        val date = DateParser.parse(details.select("div.termindetaildatumzeit").text())

        val picture = details.select("div.termindetailbeschreibung img")
        val pictureUrl = baseUrl + picture.attr("src")
        val pictureAlt = picture.attr("alt").trim()
        val pictureCopyright = picture.attr("title").trim().ifBlank { "Theater Phönix" }

        return structuredEvent(name, date) {
            withProperty(
                SemanticKeys.URL_PROPERTY,
                UrlUtils.parse(baseUrl + link.attr("rel"))
            )
            withProperty(SemanticKeys.TYPE_PROPERTY, "theater")
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, pictureAlt)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, pictureCopyright)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Theater Phönix")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf("$baseUrl/termine"))
        }
    }
}
