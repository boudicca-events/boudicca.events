package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollector
import events.boudicca.openapi.model.Event
import events.boudicca.openapi.model.EventConcert
import events.boudicca.openapi.model.EventLocation
import events.boudicca.openapi.model.RegistrationEnum
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import it.skrape.selects.DocElement
import it.skrape.selects.html5.div
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PosthofFetcher : EventCollector {
    override fun getName(): String {
        return "posthof"
    }

    override fun collectEvents(): List<Event> {
        val events = mutableListOf<Event>()
        val baseUrl = "https://www.posthof.at/programm/alles/"
        val otherUrls = mutableSetOf<String>()
        skrape(HttpFetcher) {
            request {
                url = baseUrl
            }
            response {
                htmlDocument {
                    selection("div.news-list-browse table a") {
                        findAll {
                            forEach {
                                if (it.text != "1") {
                                    otherUrls.add(it.attribute("href"))
                                }
                            }
                        }
                    }
                    parseEventList(events)
                }
            }
        }

        otherUrls.forEach {
            skrape(HttpFetcher) {
                request {
                    url = "https://posthof.at/$it"
                }
                response {
                    htmlDocument {
                        parseEventList(events)
                    }
                }
            }
        }

        return events
    }

    private fun Doc.parseEventList(events: MutableList<Event>) {
        div {
            withClass = "event-list-item"
            findAll {
                forEach {
                    val event = parseEvent(it)
                    if (event != null) {
                        events.add(event)
                    }
                }
            }
        }
    }

    private fun parseEvent(doc: DocElement): Event? {
        val event = Event().apply {
            location = EventLocation()
        }

        doc.apply {
            selection("div.h3>a") {
                findFirst {
                    event.name = text
                    event.url = "https://www.posthof.at/" + attribute("href")
                }
            }
            selection("div.news-list-subtitle") {
                findFirst {
                    event.name += " - $text"
                }
            }
            selection("span.news-list-category") {
                findFirst {
                    mapType(event, text)
                }
            }
            selection("span.news-list-date") {
                findFirst {
                    event.startDate = LocalDateTime.parse(
                        text.substring(4),
                        DateTimeFormatter.ofPattern("dd.MM.uuuu // kk:mm")
                    ).atZone(ZoneId.of("CET")).toOffsetDateTime()
                }
            }
            selection("div.news_text>p") {
                findFirst {
                    event.description = text
                }
            }
        }

        event.registration = RegistrationEnum.TICKET //are there free events in posthof?
        event.location!!.apply {
            name = "posthof"
            url = "https://www.posthof.at"
            city = "Linz"
        }
        return event
    }

    private fun mapType(event: Event, type: String) {
        val lowerType = type.lowercase()
        for (knownMusicType in KNOWN_MUSIC_TYPES) {
            if (lowerType.indexOf(knownMusicType) != -1) {
                event.type = "concert"
                event.concert = EventConcert().apply { genre = type }
                return
            }
        }
        event.type = type
    }

    private val KNOWN_MUSIC_TYPES: Set<String> = setOf(
        "metal",
        "indie",
        "pop",
        "jazz",
        "hiphop",
        "rap",
        "rock",
        "electronic",
        "punk",
        "house",
        "brass",
        "reggae",
        "soul",
        "funk",
        "folk",
        "dub",
        "klassik",        //TODO moar
    )
}
