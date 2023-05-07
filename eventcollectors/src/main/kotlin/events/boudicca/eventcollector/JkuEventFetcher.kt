package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.EventCollector
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.DocElement
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class JkuEventFetcher : EventCollector {
    override fun getName(): String {
        return "jku"
    }

    override fun collectEvents(): List<Event> {
        val eventUrls = mutableSetOf<String>()
        val icsUrls = mutableSetOf<String>()
        val baseUrl = "https://www.jku.at"
        skrape(HttpFetcher) {
            request {
                url = "${baseUrl}/studium/studieninteressierte/messen-events/"
            }
            response {
                htmlDocument {
                    div {
                        withClass = "news_list_item"
                        findAll {
                            a {
                                findAll {
                                    forEach {
                                        eventUrls.add(it.attribute("href"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        eventUrls.forEach { eventUrl ->
            skrape(HttpFetcher) {
                request {
                    url = "${baseUrl}${eventUrl}"
                }
                response {
                    htmlDocument {
                        a {
                            findAll {
                                forEach {
                                    val href = it.attribute("href")
                                    if (href.endsWith(".ics")) {
                                        icsUrls.add(href)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val events = mutableListOf<Event>()

        icsUrls.forEach {
            val fullUrl = "${baseUrl}${it}"
            val url = URL(fullUrl)
            events.addAll(parseEventFromIcs(url))
        }

        return events
    }

    fun parseEventFromIcs(icsUrl: URL): List<Event> {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        val daylongEventFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        icsUrl.openStream().use {
            val builder = CalendarBuilder()
            val calendar: Calendar = builder.build(it)
            val components = calendar.components.filterIsInstance<VEvent>()

            return components.map {
                val title = it.summary.value
                val eventName = title
                val eventStartDate = if (it.isDaylongEvent()) {
                    val dateTime = LocalDate.parse(it.startDate.value, daylongEventFormatter)
                    val zonedDateTime = ZonedDateTime.of(dateTime.atTime(0, 0), ZoneId.of("UTC"))
                    zonedDateTime.toOffsetDateTime()
                } else {
                    val dateTime = LocalDateTime.parse(it.startDate.value, formatter)
                    val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of("UTC"))
                    zonedDateTime.toOffsetDateTime()
                }

                Event(
                    eventName, eventStartDate,
                    mapOf(
                        "location.name" to it.location.value,
                        "url.ics" to icsUrl.toString(),
                        "tags" to listOf("JKU", "Universität", "Studieren").toString(),
                        "jku.uid" to it.uid.value
                    )
                )
            }
        }
    }

    private fun VEvent.isDaylongEvent(): Boolean {
        return this.startDate.toString().indexOf("VALUE=DATE") != -1
    }

}
