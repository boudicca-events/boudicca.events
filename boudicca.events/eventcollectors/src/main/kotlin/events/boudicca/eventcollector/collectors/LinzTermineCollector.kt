package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LinzTermineCollector : EventCollector {
    private val fetcher = Fetcher()
    private val LOG = LoggerFactory.getLogger(this::class.java)
    private val eventsBaseUrl = "https://www.linztermine.at/schnittstelle/downloads/events_xml.php"
    private val locationBaseUrl = "https://www.linztermine.at/schnittstelle/downloads/locations_xml.php"

    override fun getName(): String {
        return "linz termine"
    }

    override fun collectEvents(): List<Event> {
        val locations = parseLocations()
        val events = filterEvents(parseEvents())
        val eventWebsites = getEventWebsites(events)
        return mapEvents(events, locations, eventWebsites)
    }

    private fun filterEvents(events: List<LinzTermineEvent>): List<LinzTermineEvent> {
        //448552 is the locationId of casino linz, i do not want gambling in my events
        return events.filter { it.locationId != 448552 }
    }

    private fun getEventWebsites(events: List<LinzTermineEvent>): Map<String, Document> {
        //TODO we may loose some events because either they do not have a linztermine.at link or the linztermine.at link points to a 404... not sure what to do about that
        return events
            .asSequence()
            .map { it.url }
            .filter {
                it.contains("linztermine.at")
            }
            .toSet() //filter duplicates
            .mapNotNull {
                try {
                    Pair(it, Jsoup.parse(fetcher.fetchUrl(it.replace("http://", "https://"))))
                } catch (ignored: RuntimeException) {
                    //some linztermine.at links just 404 and go nowhere... not sure what this is supposed to mean
                    null
                }
            }
            .associate { Pair(it.first, it.second) }
    }

    private fun mapEvents(
        eventList: List<LinzTermineEvent>,
        locations: Map<Int, Location>,
        eventWebsites: Map<String, Document>
    ): List<Event> {
        val mappedEvents = mutableListOf<Event>()
        for (event in eventList) {
            if (event.dates.isEmpty()) {
                LOG.warn("event does not contain any dates: $event")
                continue
            }
            val website = eventWebsites[event.url] ?: continue

            var location = locations[event.locationId]
            while (location?.subOf != null && locations[location.subOf] != null) {
                location = locations[location.subOf]
            }
            val description = website.select("span.content-description").text()
            val pictureUrl = if (!website.select("div.letterbox > img").isEmpty()) {
                "https://www.linztermine.at" + website.select("div.letterbox > img").attr("src")
            } else {
                ""
            }
            val additionalProperties = mapAdditionalProperties(event)

            for (date in event.dates) {
                mappedEvents.add(
                    Event(
                        event.name,
                        date.first,
                        mapOf(
                            SemanticKeys.ENDDATE to date.second.format(DateTimeFormatter.ISO_DATE_TIME),
                            SemanticKeys.TYPE to mapEventType(event.type),
                            SemanticKeys.DESCRIPTION to description,
                            SemanticKeys.PICTURE_URL to pictureUrl,
                            SemanticKeys.REGISTRATION to (if (event.freeOfCharge) "FREE" else "TICKET"),
                            SemanticKeys.URL to event.url,
                            SemanticKeys.LOCATION_NAME to (location?.name
                                ?: event.locationFallbackName), //they do not include all locations in their location.xml files -.-
                            SemanticKeys.SOURCES to event.url + "\n" + eventsBaseUrl + "\n" + locationBaseUrl
                        ).plus(additionalProperties).filter { it.value.isNotBlank() }
                    )
                )
            }
        }
        return mappedEvents
    }

    private fun mapAdditionalProperties(event: LinzTermineEvent): Map<String, String> {
        val additionalProperties = mutableMapOf<String, String>()

        if (event.type?.first == 401) {
            additionalProperties["sport.participation"] = "watch"
        }
        if (event.type?.first == 402) {
            additionalProperties["sport.participation"] = "active"
        }

        return additionalProperties
    }

    private fun parseEvents(): List<LinzTermineEvent> {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ss")

        var date = LocalDate.now(ZoneId.of("Europe/Vienna")).atStartOfDay()
        val links = mutableListOf<String>()
        for (i in 1..(4 * 6)) {
            links.add(
                "$eventsBaseUrl?lt_datefrom=" +
                        URLEncoder.encode(
                            date.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")),
                            Charsets.UTF_8
                        )
            )
            date = date.plusWeeks(1)
        }
        return links.mapNotNull {
            try {
                loadXml(it)
            } catch (e: RuntimeException) {
                LOG.error("error fetching xml", e)
                //booooh
                null
            }
        }.flatMap {
            it.child(0).children().toList()
                .map {
                    LinzTermineEvent(
                        it.attr("id").toInt(),
                        it.select("title").text(),
                        findTag(it),
                        it.select("date").map {
                            Pair(
                                LocalDateTime.parse(it.attr("dFrom"), formatter).atZone(ZoneId.of("Europe/Vienna"))
                                    .toOffsetDateTime(),
                                LocalDateTime.parse(it.attr("dTo"), formatter).atZone(ZoneId.of("Europe/Vienna"))
                                    .toOffsetDateTime(),
                            )
                        },
                        it.attr("freeofcharge") == "1",
                        findLink(it),
                        findLocationId(it),
                        it.select("location").text()
                    )
                }
        }.distinctBy { it.id }
    }

    private fun findTag(event: Element): Pair<Int, String>? {
        val tagElement = event.select("tags").first()?.child(0)
        return if (tagElement != null) {
            Pair(tagElement.attr("id").toInt(), tagElement.text())
        } else {
            null
        }
    }

    private fun findLocationId(event: Element): Int? {
        val idAttr = event.select("location").attr("id")
        return if (!idAttr.isNullOrBlank()) {
            idAttr.toInt()
        } else {
            null
        }
    }

    private fun findLink(event: Element): String {
        var fallback: String? = null
        for (link in event.select("links link").toList()) {
            val linkUrl = link.select("url").text()
            if (!linkUrl.contains("linztermine.at")) {
                fallback = linkUrl
            } else {
                return linkUrl
            }
        }
        return fallback!!
    }

    private fun parseLocations(): Map<Int, Location> {
        val xml = loadXml(locationBaseUrl)
        return xml.child(0).children().toList().map {
            Location(
                it.attr("id").toInt(),
                it.select("name").text(),
                it.select("subof").text().toIntOrNull(),
            )
        }.associateBy { it.id }
    }

    private fun mapEventType(eventType: Pair<Int, String>?): String {
        if (eventType == null) {
            return ""
        }
        if (eventType.first == 401 || eventType.first == 402) {
            return "sport"
        }
        return eventType.second
    }

    private fun loadXml(s: String): Document {
        return Jsoup.parse(fetcher.fetchUrl(s), Parser.xmlParser())
    }

    data class Location(
        val id: Int,
        val name: String,
        val subOf: Int?,
    )

    data class LinzTermineEvent(
        val id: Int,
        val name: String,
        val type: Pair<Int, String>?,
        val dates: List<Pair<OffsetDateTime, OffsetDateTime>>,
        val freeOfCharge: Boolean,
        val url: String,
        val locationId: Int?,
        val locationFallbackName: String,
    )
}