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
import events.boudicca.eventcollector.util.fetchUrlAndParse
import events.boudicca.eventcollector.util.withDescription

class TheaterInDerInnenstadtCollector : TwoStepEventCollector<String>("theaterinderinnenstadt") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://theater-innenstadt.at/"
    private val eventUrl = baseUrl + "spielplan/"

    override fun getAllUnparsedEvents(): List<String> {
        val document = fetcher.fetchUrlAndParse(eventUrl)
        val eventLinks = document.select("#main-content div.flex.flex-col > a").map { it.attr("href") }
        return eventLinks
            // they have foreign urls we cannot parse, so only accept relative ones
            .filter { !it.startsWith("http") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val eventSite = fetcher.fetchUrlAndParse(baseUrl + event.removePrefix("/"))
        val title = eventSite.title()
        val lastTitleDash = title.lastIndexOf('–')
        val name = title.substring(0, lastTitleDash).trim()
        val dateTimeString = title.substring(lastTitleDash).trim()
        val startDateTime = DateParser.parse(dateTimeString)

        val description = eventSite.select("div.prose")

        val imgSrc = eventSite.select("aside img").attr("src")

        val type: String? =
            when {
                description.text().lowercase().contains("musical") -> "musical"
                description.text().lowercase().contains("comedy") -> "comedy"
                description.text().lowercase().contains("theater") -> "theater"
                else -> null
            }

        return structuredEvent(name, startDateTime) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(eventUrl))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withDescription(description)
            withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.ART)
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(baseUrl, imgSrc))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Theater in der Innenstadt")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Theater in der Innenstadt")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.TICKET)
        }
    }
}
