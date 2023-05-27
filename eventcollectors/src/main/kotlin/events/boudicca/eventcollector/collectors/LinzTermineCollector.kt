package events.boudicca.eventcollector.collectors

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.EventCollector
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LinzTermineCollector : EventCollector {
    override fun getName(): String {
        return "Linz Termine"
    }

    override fun collectEvents(): List<events.boudicca.api.eventcollector.Event> {
        val locations = parseLocations()
        val events = parseEvents()
        val eventWebsites = getEventWebsites(events)
        return mapEvents(events, locations, eventWebsites)
    }

    private fun getEventWebsites(events: List<Event>): Map<Int, Document> {
        //TODO we may loose some events because either they do not have a linztermine.at link or the linztermine.at link points to a 404... not sure what to do about that
        return events
            .filter {
                it.url.contains("linztermine.at")
            }
            .map {
                try {
                    Pair(it.id, Jsoup.connect(it.url).get() as Document?)
                } catch (ignored: HttpStatusException) {
                    //some linztermine.at links just 404 and go nowhere... not sure what this is supposed to mean
                    Pair(it.id, null as Document?)
                }
            }
            .filter { it.second != null }
            .associate { Pair(it.first, it.second!!) }
    }

    private fun mapEvents(
        eventList: List<Event>,
        locations: Map<Int, Location>,
        eventWebsites: Map<Int, Document>
    ): List<events.boudicca.api.eventcollector.Event> {
        val mappedEvents = mutableListOf<events.boudicca.api.eventcollector.Event>()
        for (event in eventList) {
            if (event.dates.isEmpty()) {
                println("event does not contain any dates: $event")
                continue
            }

            val location = locations[event.locationId]
            val website = eventWebsites[event.id]
            if (website == null) {
                continue
            }
            val description = website.select("span.content-description").text()
            val pictureUrl = if (!website.select("div.letterbox > img").isEmpty()) {
                "https://www.linztermine.at" + website.select("div.letterbox > img").attr("src")
            } else {
                ""
            }
            for (date in event.dates) {
                mappedEvents.add(
                    Event(
                        event.name,
                        date.first.toOffsetDateTime(),
                        mapOf(
                            SemanticKeys.ENDDATE to date.second.format(DateTimeFormatter.ISO_DATE_TIME),
                            SemanticKeys.TYPE to (event.type ?: ""),
                            SemanticKeys.DESCRIPTION to description,
                            SemanticKeys.PICTUREURL to pictureUrl,
                            SemanticKeys.REGISTRATION to (if (event.freeOfCharge) "FREE" else "TICKET"),
                            SemanticKeys.URL to event.url,
                            SemanticKeys.LOCATION_NAME to (location?.name
                                ?: event.locationFallbackName), //they do not include all locations in their location.xml files -.-
                            SemanticKeys.LOCATION_CITY to (location?.city ?: ""),
                            SemanticKeys.LOCATION_URL to (location?.url ?: ""),
                        ).filter { it.value.isNotBlank() }
                    )
                )
            }
        }
        return mappedEvents
    }

    private fun parseEvents(): List<Event> {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ss")
        val xml = loadXml("https://www.linztermine.at/schnittstelle/downloads/events_xml.php") //TODO more then 7 days?
        return xml.child(0).children().toList()
            .map {
                Event(
                    it.attr("id").toInt(),
                    it.select("title").text(),
                    it.select("tags").first()?.child(0)?.text(),
                    it.select("date").map {
                        Pair(
                            LocalDateTime.parse(it.attr("dFrom"), formatter).atZone(ZoneId.of("CET")),
                            LocalDateTime.parse(it.attr("dTo"), formatter).atZone(ZoneId.of("CET")),
                        )
                    },
                    it.attr("freeofcharge") == "1",
                    findLink(it),
                    it.select("location").attr("id").toInt(),
                    it.select("location").text()
                )
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
        val xml = loadXml("https://www.linztermine.at/schnittstelle/downloads/locations_xml.php")
        return xml.child(0).children().toList().filter {
            if (it.tagName() == "location") {
                true
            } else if (it.tagName() == "site") {
                //sites are ignored
                false
            } else {
                throw IllegalArgumentException("unknown tag ${it.tagName()}")
            }
        }.map {
            Location(
                it.attr("id").toInt(),
                it.select("name").text(),
                it.select("city").text(),
                it.select("link").text()
            )
        }.associateBy { it.id }
    }

    private fun loadXml(s: String): Document {
        return Jsoup.connect(s).parser(Parser.xmlParser()).get()
    }

    data class Location(
        val id: Int,
        val name: String,
        val city: String,
        val url: String
    )

    data class Event(
        val id: Int,
        val name: String,
        val type: String?,
        val dates: List<Pair<ZonedDateTime, ZonedDateTime>>,
        val freeOfCharge: Boolean,
        val url: String,
        val locationId: Int,
        val locationFallbackName: String,
    )
}