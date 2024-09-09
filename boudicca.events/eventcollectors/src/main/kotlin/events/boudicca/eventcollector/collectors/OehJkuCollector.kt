package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class OehJkuCollector : TwoStepEventCollector<String>("oehjku") {

    private val fetcher = Fetcher()
    private val baseUrl = "https://oeh.jku.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "veranstaltungen"))
        return document.select("div.node-event a")
            .map { it.attr("href") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(baseUrl + event))

        val name = eventSite.select("h1").text()
        val startDate = parseDate(eventSite)

        val description = eventSite.select("div.generic-two-column-stacked-region--footer").text()

        val location = eventSite.select("div.generic-two-column-stacked-region--second").text()
            .removePrefix("Ort: ")

        return StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(baseUrl + event))
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, location)
            .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.jku.at/"))
            .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl + event))
            .build()
    }

    private fun parseDate(element: Element): OffsetDateTime {
        val fullDateTime = element.select("div.pane-node-field-event-date").text().split(" - ")
        val date = fullDateTime[0].split(", ")[1]
        val time = fullDateTime[1].split(" bis ")[0]

        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("d. MMMM uuuu", Locale.GERMAN))
        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("k:mm"))

        return localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
