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

class ViperRoomCollector : TwoStepEventCollector<String>("viperroom") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val eventsList = Jsoup.parse(fetcher.fetchUrl("https://www.viper-room.at/veranstaltungen"))

        return eventsList.select("ul.events_list div.event_actions a:nth-child(1)")
            .map { it.attr("href") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("h1.entry-title").text()
        val startDate = parseDate(eventSite)

        val description = eventSite.select("div#em-event-6").first()!!
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
        val pictureUrl = if (!img.isEmpty()) {
            UrlUtils.parse(img.first()!!.attr("src"))
        } else {
            null
        }

        return StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            .withProperty(SemanticKeys.TYPE_PROPERTY, "concert")
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Viper Room")
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            .build()
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
