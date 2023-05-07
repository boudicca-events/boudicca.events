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
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class ZuckerfabrikFetcher : EventCollector {
    override fun getName(): String {
        return "zuckerfabrik"
    }

    override fun collectEvents(): List<Event> {
        val events = mutableListOf<Event>()
        val baseUrl = "https://www.zuckerfabrik.at/termine-tickets/"
        val eventUrls = mutableSetOf<String>()
        skrape(HttpFetcher) {
            request {
                url = baseUrl
            }
            response {
                htmlDocument {
                    selection("div#storycontent > a.bookmarklink") {
                        findAll {
                            forEach {
                                eventUrls.add(it.attribute("href"))
                            }
                        }
                    }
                }
            }
        }

        eventUrls.forEach {
            skrape(HttpFetcher) {
                request {
                    url = it
                }
                response {
                    htmlDocument {
                        val event = parseEvent(it, this)
                        if (event != null) {
                            events.add(event)
                        }
                    }
                }
            }
        }

        return events
    }

    private fun parseEvent(url: String, doc: Doc): Event? {
        var name: String? = null
        var startDate: OffsetDateTime? = null
        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = url

        doc.apply {
            selection("div#storycontent>h2") {
                findFirst {
                    name = text
                }
            }
            selection("div#storycontent>p") {
                findAll {
                    get(0).let {
                        if (it.text.isNotBlank()) {
                            name += " - " + it.text
                        }
                    }
                    get(1).let {
                        val split = it.text.split(" am ")
                        data[SemanticKeys.TYPE] = split[0]
                        val dateSplit = split[1].split(",").map { it.trim() }
                        val date = LocalDate.parse(
                            dateSplit[1],
                            DateTimeFormatter.ofPattern("d. LLLL uuuu").withLocale(Locale.GERMAN)
                        )
                        var startTimeString = dateSplit[2].substring(0, dateSplit[2].length - 4)
                        val startTime: LocalTime
                        var endTime: LocalTime? = null
                        val timeFormatter = DateTimeFormatter.ofPattern("kk:mm")
                        if (dateSplit[2].contains(" - ")) {
                            val timeSplit = startTimeString.split(" - ")
                            startTimeString = timeSplit[0]
                            endTime = LocalTime.parse(timeSplit[1], timeFormatter)
                        }
                        startTime = LocalTime.parse(startTimeString, timeFormatter)
                        startDate = date.atTime(startTime).atZone(ZoneId.of("CET")).toOffsetDateTime()
                        if (endTime != null) {
                            data[SemanticKeys.ENDDATE] =
                                date.atTime(startTime).atZone(ZoneId.of("CET")).toOffsetDateTime().toString()
                        }
                    }
                    data[SemanticKeys.DESCRIPTION] = (2 until this.size).map { this.get(it) }.joinToString("\n")
                }
            }
        }

        data["location.name"] = "Zuckerfabrik"
        data["location.url"] = "https://www.zuckerfabrik.at"
        data["location.city"] = "Enns"
        if (name != null && startDate != null) {
            return Event(name!!, startDate!!, data)
        }
        return null
    }

}
