package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.annotations.BoudiccaEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.EventCategory
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

@BoudiccaEventCollector("theaterinderinnenstadt")
class TheaterInDerInnenstadtCollector : TwoStepEventCollector<Pair<Element, String?>>("theaterinderinnenstadt") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://theater-innenstadt.at/"
    private val eventUrl = baseUrl + "spielplan/"

    override fun getAllUnparsedEvents(): List<Pair<Element, String?>> {
        val document = Jsoup.parse(fetcher.fetchUrl(eventUrl))
        val events = document.select("div.event")
        val logoSrc = document.selectFirst("a.tm-logo img")?.attr("src")
        return events.map { Pair(it, logoSrc) }
    }

    override fun parseMultipleStructuredEvents(event: Pair<Element, String?>): List<StructuredEvent?> {
        val (eventSite, logoSrc) = event
        val name = eventSite.select("span.evcal_event_title").text()
        val description = eventSite.select("[itemprop=description]").text()

        val dateInfos = eventSite.select("span.evoet_dayblock")
        val date = dateInfos.select(".date").text()
        val month = dateInfos.select(".month").text()
        val year = dateInfos.attr("data-syr")
        val startDate = "$date.$month $year"
        val startTime = dateInfos.select(".time").text()
        val startDateTime = DateParser.parse(startDate, startTime)

        var imgSrc = eventSite.select(".evocard_main_image img").attr("src")
        if (imgSrc.isBlank() && !logoSrc.isNullOrBlank()) {
            imgSrc = logoSrc
        }

        val type: String? =
            when {
                description.lowercase().contains("theater") -> "theater"
                description.lowercase().contains("musical") -> "musical"
                description.lowercase().contains("comedy") -> "comedy"
                else -> null
            }

        return structuredEvent(name, startDateTime) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(eventUrl))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.ART)
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgSrc))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Theater in der Innenstadt")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Theater in der Innenstadt")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.TICKET)
        }
    }
}
