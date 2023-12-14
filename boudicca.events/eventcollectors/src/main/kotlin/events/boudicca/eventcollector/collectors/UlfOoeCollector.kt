package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class UlfOoeCollector : TwoStepEventCollector<String>("ulfooe") {

    private val LOG = LoggerFactory.getLogger(this::class.java)
    private val fetcher = Fetcher()
    private val baseUrl = "https://www.ulf-ooe.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val fetcher = Fetcher()
        val events = mutableListOf<Element>()

        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "veranstaltungskalender?page=1"))
        val otherUrls = document.select("nav.pagination ul li a")
            .toList()
            .map { it.attr("href") }
        parseEventList(document, events)

        otherUrls.forEach {
            parseEventList(Jsoup.parse(fetcher.fetchUrl(baseUrl + it)), events)
        }

        return events.map { it.attr("href") }
    }

    private fun parseEventList(document: Document, events: MutableList<Element>) {
        events.addAll(document.select("a.event.event_list_item.event_list_item_link")
            .toList()
            .filter { !it.attr("href").startsWith("http") }) // exclude events from others than ulf
    }

    override fun parseEvent(event: String): Event? {
        val fullEventLink = baseUrl + event
        val eventSite = Jsoup.parse(fetcher.fetchUrl(fullEventLink))

        val name = eventSite.select("h1").text()

        val startDate = try {
            parseDate(eventSite)
        } catch (exc: java.time.format.DateTimeParseException) {
            LOG.info("Error in ${fullEventLink}: can't parse date, might be a multi-day event")
            return null
        }

        val data = mutableMapOf<String, String>()

        data[SemanticKeys.URL] = fullEventLink
        data[SemanticKeys.DESCRIPTION] = eventSite.select("div.field-text").text()

        val locationName = eventSite.select("div.location").not("div.location.link-google-maps").text()
        if (locationName.isNotEmpty()) {
            val regex = """(?<name>.*?)[,|\s]*(?<zip>\d{4}) (?<city>[\w\s]+)""".toRegex()
            val matchResult = regex.find(locationName)
            if (matchResult != null) {
                data[SemanticKeys.LOCATION_NAME] = matchResult.groups["name"]!!.value.trimEnd()
                data[SemanticKeys.LOCATION_CITY] = matchResult.groups["city"]!!.value.trimEnd()
            } else {
                data[SemanticKeys.LOCATION_NAME] = locationName
            }
        }

        val img = eventSite.select("div.event picture img")
        if (!img.isEmpty()) {
            data[SemanticKeys.PICTUREURL] = baseUrl + img.first()!!.attr("src")
        }
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!
        return Event(name, startDate, data)
    }

    private fun parseDate(element: Element): OffsetDateTime {
        val dtDiv = element.select("div.details_date_time")
        val date = dtDiv.select("div.date").text()
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd. MMMM uuuu", Locale.GERMAN))

        var time = dtDiv.select("div.time").text()
        var localDateTime = localDate.atStartOfDay()
        if (time.isNotEmpty()) {
            time = time.split(" ")[0]
            val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("kk:mm"))
            localDateTime = localDate.atTime(localTime)
        }
        return localDateTime.atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }
}
