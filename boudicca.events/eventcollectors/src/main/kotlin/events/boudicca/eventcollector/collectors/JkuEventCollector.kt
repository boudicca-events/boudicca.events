package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Event
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.Fetcher
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import org.jsoup.Jsoup
import java.net.URI
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class JkuEventCollector : EventCollector {
    override fun getName(): String {
        return "jku"
    }

    override fun collectEvents(): List<Event> {
        val fetcher = Fetcher()
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.jku.at/studium/studieninteressierte/messen-events/"))
        val eventUrls = document.select("div.news_list_item a").eachAttr("href")

        val icsUrls = eventUrls
            .flatMap {
                val eventJson = Jsoup.parse(fetcher.fetchUrl("https://www.jku.at$it"))
                eventJson.select("a").eachAttr("href")
            }
            .filter { it.endsWith(".ics") }


        val events = mutableListOf<Event>()

        icsUrls.forEach {
            val fullUrl = "https://www.jku.at${it}"
            val url = URI(fullUrl).toURL()
            events.addAll(parseEventFromIcs(url))
        }

        return events
    }

    private fun parseEventFromIcs(icsUrl: URL): List<Event> {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        val daylongEventFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        icsUrl.openStream().use { inputStream ->
            val builder = CalendarBuilder()
            val calendar: Calendar = builder.build(inputStream)
            val components = calendar.components.filterIsInstance<VEvent>()

            return components.map { vEvent ->
                val eventName = vEvent.summary.value
                val eventStartDate = if (vEvent.isDaylongEvent()) {
                    val dateTime = LocalDate.parse(vEvent.startDate.value, daylongEventFormatter)
                    val zonedDateTime = ZonedDateTime.of(dateTime.atTime(0, 0), ZoneId.of("UTC"))
                    zonedDateTime.toOffsetDateTime()
                } else {
                    val dateTime = LocalDateTime.parse(vEvent.startDate.value, formatter)
                    val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of("UTC"))
                    zonedDateTime.toOffsetDateTime()
                }

                Event(
                    eventName, eventStartDate,
                    mapOf(
                        base.boudicca.SemanticKeys.LOCATION_NAME to vEvent.location.value,
                        base.boudicca.SemanticKeys.TAGS to listOf("JKU", "Universität", "Studieren").toString(),
                        "url.ics" to icsUrl.toString(),
                        "jku.uid" to vEvent.uid.value
                    )
                )
            }
        }
    }

    private fun VEvent.isDaylongEvent(): Boolean {
        return this.startDate.toString().indexOf("VALUE=DATE") != -1
    }

}
