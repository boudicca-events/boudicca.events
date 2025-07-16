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

class MuseumArbeitsweltCollector : TwoStepEventCollector<Pair<String, String>>("museumArbeitswelt") {
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<Pair<String, String>> {
        val events = mutableListOf<Pair<String, String>>()
        val document = Jsoup.parse(fetcher.fetchUrl("https://museumarbeitswelt.at/kalender/"))
        document.select("div.ecs-event").forEach() {
            events.add(
                Pair(
                    it.select("a.act-view-more").attr("href"),
                    it.select("div.decm-show-detail-center").text()
                )
            )
        }
        return events
    }

    override fun parseMultipleStructuredEvents(event: Pair<String, String>): List<StructuredEvent?>? {
        val (eventUrl, dateToParse) = event
        val eventSite = Jsoup.parse(fetcher.fetchUrl(eventUrl))

        val name = eventSite.select("h1.entry-title").text()
        val startDate = parseDate(dateToParse)

        val description = eventSite.select("div.et_pb_post_content").text()

        val img = eventSite.select("div.et_pb_title_featured_container span.et_pb_image_wrap img")
        val pictureUrl = if (!img.isEmpty()) {
            img.last()!!.attr("src")
        } else {
            null
        }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(eventUrl))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(eventUrl))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                if (pictureUrl != null) UrlUtils.parse(pictureUrl) else null
            )
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Museum Arbeitswelt")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://museumarbeitswelt.at/"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Steyr")
        }
    }

    private fun parseDate(dateToParse: String): DateParserResult {
        return DateParser.parse(dateToParse)
    }

}
