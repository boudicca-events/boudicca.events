package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class StadtwerkstattCollector : TwoStepEventCollector<String>("stadtwerkstatt") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://club.stwst.at/"))
        return document.select("div.single-event a")
            .map { it.attr("href") }
    }

    override fun parseEvent(event: String): Event {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        var name = eventSite.select("li.event-title").text()
        if (name.isBlank()) {
            name = eventSite.select("ul.event-artists span.name").joinToString(", ") { it.text().trim() }
        }
        val startDate = parseDate(eventSite)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = event
        val type = eventSite.select("div.genre").text()
        if (type.isNotBlank()) {
            data[SemanticKeys.TYPE] = type
        }
        val description = eventSite.select("div.event-text").text()
        if (description.isNotBlank()) {
            data[SemanticKeys.DESCRIPTION] = description
        }

        val img = eventSite.select("div.event-text img")
        if (!img.isEmpty()) {
            data[SemanticKeys.PICTUREURL] = img.first()!!.attr("src")
        }

        data[SemanticKeys.LOCATION_NAME] = "Stadtwerkstatt"
        data[SemanticKeys.LOCATION_URL] = "https://club.stwst.at"
        data[SemanticKeys.LOCATION_CITY] = "Linz"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY] = "true"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLESEATS] = "true"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLETOILETS] = "true"
        //TODO could parse lineup

        return Event(name, startDate, data)
    }

    private fun parseDate(element: Element): OffsetDateTime {
        val fullDate = element.select("div.date").text()
        val date = fullDate.split(", ")[1]
        val locationAndTime = element.select("div.location_time").text()
        val time = locationAndTime.split(", ")[1]

        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.LL.uuuu"))
        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("kk:mm"))

        return localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
