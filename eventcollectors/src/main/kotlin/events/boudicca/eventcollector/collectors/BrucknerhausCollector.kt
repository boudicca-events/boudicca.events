package events.boudicca.eventcollector.collectors

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.TwoStepEventCollector
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import it.skrape.selects.DocElement
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class BrucknerhausCollector : TwoStepEventCollector<DocElement>("brucknerhaus") {

    override fun getAllUnparsedEvents(): List<DocElement> {
        val events = mutableListOf<DocElement>()
        val baseUrl = "https://www.brucknerhaus.at/programm/veranstaltungen"
        val otherUrls = mutableListOf<String>()
        skrape(HttpFetcher) {
            request {
                url = baseUrl
            }
            response {
                htmlDocument {
                    selection("ul.pagination>li") {
                        findAll {
                            otherUrls.addAll(
                                filter { it.attribute("class").isEmpty() }
                                    .map { "https://www.brucknerhaus.at" + it.children.first().attribute("href") }
                            )
                        }
                    }
                    events.addAll(findUnparsedEvents(this))
                }
            }
        }

        otherUrls.forEach {
            skrape(HttpFetcher) {
                request {
                    url = it
                }
                response {
                    htmlDocument {
                        events.addAll(findUnparsedEvents(this))
                    }
                }
            }
        }

        return events
    }

    private fun findUnparsedEvents(doc: Doc): List<DocElement> {
        val events = mutableListOf<DocElement>()
        doc.selection("div.event div.event__element") {
            findAll {
                events.addAll(this)
            }
        }
        return events
    }

    override fun parseEvent(event: DocElement): Event? {
        var name: String? = null
        val startDate: OffsetDateTime? = parseDate(event)
        val data = mutableMapOf<String, String>()

        event.apply {
            selection("a.headline_link") {
                findFirst {
                    data[SemanticKeys.URL] = "https://www.brucknerhaus.at" + attribute("href")
                }
            }
            selection("div.event__name") {
                findFirst {
                    name = text
                }
            }
            selection("div.event__image img") {
                findFirst {
                    data["pictureUrl"] = attribute("src")
                }
            }
            try {
                selection("div.event__teaser p") {
                    findFirst {
                        data[SemanticKeys.DESCRIPTION] = text
                    }
                }
            } catch (ignored: Exception) {
                try {
                    selection("div.event__teaser .fr-view") {
                        findFirst {
                            data[SemanticKeys.DESCRIPTION] = children.first().text
                        }
                    }
                } catch (ignored2: Exception) {
                    //TODO some just don't have a small description...
                }
            }
        }

        data[SemanticKeys.TYPE] = "concert" //TODO check
        data[SemanticKeys.LOCATION_NAME] = "Brucknerhaus" //TODO not all events are there...
        data[SemanticKeys.LOCATION_URL] = "https://www.brucknerhaus.at/"
        data[SemanticKeys.LOCATION_CITY] = "Linz"

        if (name != null && startDate != null) {
            return Event(name!!, startDate!!, data)
        }
        return null
    }

    private fun parseDate(event: DocElement): OffsetDateTime? {
        var datePart1: String? = null
        var datePart2: String? = null
        var datePart3: String? = null
        var time: String? = null

        event.selection("div.event__date") {
            findFirst {
                datePart1 = children.get(1).children.get(0).text
                datePart2 = children.get(1).children.get(1).text
                datePart3 = children.get(2).children.get(0).text
            }
        }
        event.selection("div.event__location") {
            findFirst {
                time = children.get(0).children.get(0).text
            }
        }

        val localDate = LocalDate.parse(
            datePart1!! + " " + mapMonth(datePart2!!) + " " + datePart3!!,
            DateTimeFormatter.ofPattern("d M uu").withLocale(Locale.GERMAN)
        )
        val localTime = LocalTime.parse(time!!, DateTimeFormatter.ofPattern("kk:mm"))

        return localDate.atTime(localTime).atZone(ZoneId.of("CET")).toOffsetDateTime()
    }

    private fun mapMonth(month: String): String {
        return when (month.uppercase()) {
            "JAN" -> "1"
            "FEB" -> "2"
            "MÄRZ" -> "3"
            "APRIL" -> "4"
            "MAI" -> "5"
            "JUNI" -> "6"
            "JULI" -> "7"
            "AUG" -> "8"
            "SEP" -> "9"
            "OKT" -> "10"
            "NOV" -> "11"
            "DEZ" -> "12"
            else -> throw IllegalArgumentException("cannot map month $month")
        }
    }

}
