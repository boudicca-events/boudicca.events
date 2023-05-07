package events.boudicca.eventcollector

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.EventCollector
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
        var name: String? = null
        var startDate: ZonedDateTime? = null
        val data = mutableMapOf<String, String>()

        doc.apply {
            selection("div.h3>a") {
                findFirst {
                    name = text
                    data[SemanticKeys.URL] = "https://www.posthof.at/" + attribute("href")
                }
            }
            selection("div.news-list-subtitle") {
                findFirst {
                    name += " - $text"
                }
            }
            selection("span.news-list-category") {
                findFirst {
                    mapType(data, text)
                }
            }
            selection("span.news-list-date") {
                findFirst {
                    startDate = LocalDateTime.parse(
                        text.substring(4),
                        DateTimeFormatter.ofPattern("dd.MM.uuuu // kk:mm")
                    ).atZone(ZoneId.of("CET"))
                }
            }
            selection("div.news_text>p") {
                findFirst {
                    data[SemanticKeys.DESCRIPTION] = text
                }
            }
        }

        data[SemanticKeys.REGISTRATION] = "ticket" //are there free events in posthof?
        data[SemanticKeys.LOCATION_NAME] = "posthof"
        data[SemanticKeys.LOCATION_URL] = "https://www.posthof.at"
        data[SemanticKeys.LOCATION_CITY] = "Linz"
        if (name != null && startDate != null) {
            return Event(name!!, startDate!!.toOffsetDateTime(), data)
        }
        return null
    }

    private fun mapType(data: MutableMap<String, String>, type: String) {
        val lowerType = type.lowercase()
        for (knownMusicType in KNOWN_MUSIC_TYPES) {
            if (lowerType.indexOf(knownMusicType) != -1) {
                data[SemanticKeys.TYPE] = "concert"
                data[SemanticKeys.CONCERT_GENRE] = type
                return
            }
        }
        data[SemanticKeys.TYPE] = type
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
