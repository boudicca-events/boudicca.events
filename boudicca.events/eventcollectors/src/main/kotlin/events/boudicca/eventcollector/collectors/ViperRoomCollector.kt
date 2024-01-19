package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ViperRoomCollector : TwoStepEventCollector<String>("viperroom") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val eventsList = Jsoup.parse(fetcher.fetchUrl("https://www.viper-room.at/veranstaltungen"))

        return eventsList.select("ul.events_list div.event_actions a:nth-child(1)")
            .map { it.attr("href") }
    }

    override fun parseEvent(event: String): Event {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("h1.entry-title").text()
        val startDate = parseDate(eventSite)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = event
        data[SemanticKeys.TYPE] = "concert"
        data[SemanticKeys.DESCRIPTION] = eventSite.select("div#em-event-6").first()!!
            .children()
            .toList()
            .filter {
                (it.tagName() == "div" &&
                        !(it.classNames().contains("event_price") || it.classNames().contains("event_actions")))
                        ||
                        (it.tagName() == "p" && !it.classNames().contains("event_time"))
            }
            .map { it.text() }
            .filter { it.isNotBlank() }
            .joinToString("\n")

        val img = eventSite.select("div#em-event-6 p img")
        if (!img.isEmpty()) {
            data[SemanticKeys.PICTUREURL] = img.first()!!.attr("src")
        }

        data[SemanticKeys.LOCATION_NAME] = "Viper Room"
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

        return Event(name, startDate, data)
    }

    private fun parseDate(event: Element): OffsetDateTime {

        val fullDateText = event.select("p.event_time").textNodes()[0].text()
        val dateText = fullDateText.split(", ")[1].trim()

        val fullTimeText = event.select("span.event_doors").text()
        val timeText = fullTimeText.removePrefix("Doors open ").trim()

        val localDate = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.GERMAN))
        val localTime = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("kk:mm"))

        return localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }
}
