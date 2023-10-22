package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.Event
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
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

    override fun parseEvent(event: String): Event {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(baseUrl + event))

        val name = eventSite.select("h1").text()
        val startDate = parseDate(eventSite)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = baseUrl + event

        val description = eventSite.select("div.generic-two-column-stacked-region--footer").text()
        if (description.isNotBlank()) {
            data[SemanticKeys.DESCRIPTION] = description
        }

        val location = eventSite.select("div.generic-two-column-stacked-region--second").text()
            .removePrefix("Ort: ")
        if (location.isNotBlank()) {
            data[SemanticKeys.LOCATION_NAME] = location
            data[SemanticKeys.LOCATION_URL] = "https://www.jku.at/"
            data[SemanticKeys.LOCATION_CITY] = "Linz"
        }

        return Event(name, startDate, data)
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
