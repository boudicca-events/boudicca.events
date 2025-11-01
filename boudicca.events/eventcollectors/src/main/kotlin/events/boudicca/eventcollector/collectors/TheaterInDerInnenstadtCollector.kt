package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.EventCategory
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class TheaterInDerInnenstadtCollector : TwoStepEventCollector<Element>("theaterinderinnenstadt") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://theater-innenstadt.at/"
    private val eventUrl = baseUrl + "spielplan/"

    override fun getAllUnparsedEvents(): List<Element> {
        val document = Jsoup.parse(fetcher.fetchUrl(eventUrl))
        val events = document.select("div.event")
        val logo = document.selectFirst("a.tm-logo img")
        events.forEach { it.append(logo.toString()) }
        return events
    }

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent?>? {
        val name = event.select("span.evcal_event_title").text()
        val description = event.select("[itemprop=description]").text()

        val dateInfos = event.select("span.evoet_dayblock")
        val date = dateInfos.select(".date").text()
        val month = dateInfos.select(".month").text()
        val year = dateInfos.attr("data-syr")
        val startDate = "$date.$month $year"
        val startTime = dateInfos.select(".time").text()
        val startDateTime = DateParser.parse(startDate, startTime)

        var imgSrc = event.select(".evocard_main_image img").attr("src")
        if (imgSrc.isBlank()) {
            imgSrc = event.select("img").attr("src")
        }

        var type: String? = null
        if (description.lowercase().contains("theater")) {
            type = "theater"
        } else if (description.lowercase().contains("musical")) {
            type = "musical"
        } else if (description.lowercase().contains("comedy")) {
            type = "comedy"
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
