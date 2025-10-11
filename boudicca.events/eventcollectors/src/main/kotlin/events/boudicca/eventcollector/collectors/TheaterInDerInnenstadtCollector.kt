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
        return Jsoup
            .parse(fetcher.fetchUrl(eventUrl))
            .select("div.event")
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

        val imgSrc = event.select(".evocard_main_image img").attr("src")

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
            if (imgSrc.isNotBlank()) withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgSrc))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Theater in der Innenstadt")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.TICKET)
        }
    }
}
