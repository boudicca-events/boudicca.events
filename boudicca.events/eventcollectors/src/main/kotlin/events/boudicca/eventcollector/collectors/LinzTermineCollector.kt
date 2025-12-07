package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.Registration
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.StructuredEventBuilder
import base.boudicca.model.structured.dsl.structuredEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.net.URLEncoder
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LinzTermineCollector : EventCollector {
    private val fetcher = FetcherFactory.newFetcher()
    private val logger = KotlinLogging.logger {}
    private val eventsBaseUrl = "https://www.linztermine.at/schnittstelle/downloads/events_xml.php"
    private val locationBaseUrl = "https://www.linztermine.at/schnittstelle/downloads/locations_xml.php"

    override fun getName(): String {
        return "linz termine"
    }

    override fun collectStructuredEvents(): List<StructuredEvent> {
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
                } catch (_: RuntimeException) {
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
    ): List<StructuredEvent> {
        val mappedEvents = eventList.flatMap { event ->
            if (event.dates.isEmpty()) {
                logger.warn { "event does not contain any dates: $event" }
                return@flatMap emptyList()
            }
            val website = eventWebsites[event.url] ?: return@flatMap emptyList()

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

            event.dates.map { date ->
                structuredEvent(event.name, date.first) {
                    withCommonProperties(event, description, pictureUrl, location)
                    withProperty(SemanticKeys.ENDDATE_PROPERTY, date.second)
                }
            }
        }
        return mappedEvents
    }

    private fun StructuredEventBuilder.withCommonProperties(
        event: LinzTermineEvent,
        description: String?,
        pictureUrl: String,
        location: Location?
    ) {
        withProperty(SemanticKeys.TYPE_PROPERTY, mapEventType(event.type))
        withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
        withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
        withProperty(
            SemanticKeys.REGISTRATION_PROPERTY,
            (if (event.freeOfCharge) Registration.FREE else Registration.TICKET)
        )
        withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event.url))
        withProperty(
            SemanticKeys.LOCATION_NAME_PROPERTY,
            (location?.name ?: event.locationFallbackName)
        ) //they do not include all locations in their location.xml files -.-
        withProperty(
            SemanticKeys.LOCATION_CITY_PROPERTY,
            (location?.city)
        )
        withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event.url, eventsBaseUrl, locationBaseUrl))
        withAdditionalProperties(event)
    }

    private fun StructuredEventBuilder.withAdditionalProperties(event: LinzTermineEvent) {
        //TODO make a semantic key out of this
        if (event.type?.first == 401) {
            this.withKeyValuePair(Key.builder("sport.participation").build(), "watch")
        }
        if (event.type?.first == 402) {
            this.withKeyValuePair(Key.builder("sport.participation").build(), "active")
        }
    }

    private fun parseEvents(): List<LinzTermineEvent> {
        val urlFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")

        var date = LocalDate.now(ZoneId.of("Europe/Vienna")).atStartOfDay()
        val links = mutableListOf<String>()
        for (ignored in 1..(4 * 6)) {
            links.add(
                "$eventsBaseUrl?lt_datefrom=" +
                        URLEncoder.encode(
                            date.format(urlFormatter),
                            Charsets.UTF_8
                        )
            )
            date = date.plusWeeks(1)
        }
        return links.mapNotNull {
            try {
                loadXml(it)
            } catch (e: RuntimeException) {
                logger.error(e) { "error fetching xml" }
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
                                DateParser.parse(it.attr("dFrom")).single().startDate,
                                DateParser.parse(it.attr("dTo")).single().startDate
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
        return if (idAttr.isNotBlank()) {
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
                it.select("city").text(),
                it.select("subof").text().toIntOrNull(),
            )
        }.associateBy { it.id }
    }

    private fun mapEventType(eventType: Pair<Int, String>?): String {
        return when {
            eventType == null -> ""
            eventType.first == 401 || eventType.first == 402 -> "sport"
            else -> eventType.second
        }
    }

    private fun loadXml(s: String): Document {
        return Jsoup.parse(fetcher.fetchUrl(s), Parser.xmlParser())
    }

    data class Location(
        val id: Int,
        val name: String,
        val city: String?,
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
