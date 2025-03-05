package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.modify
import base.boudicca.model.structured.dsl.structuredEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class FuerUnsCollector : TwoStepEventCollector<String>("fueruns") {

    private val logger = KotlinLogging.logger {}
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.fuer-uns.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val events = mutableListOf<Element>()

        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "aktuelle-veranstaltungen/veranstaltungskalender?page=1"))
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
        events.addAll(
            document.select("a.event.event_list_item.event_list_item_link")
                .toList()
                .filter { !it.attr("href").startsWith("http") }) // exclude events from others than fuer uns
    }

    override fun parseStructuredEvent(eventUrl: String): StructuredEvent? {
        val fullEventLink = baseUrl + eventUrl
        val eventSite = Jsoup.parse(fetcher.fetchUrl(fullEventLink))

        val name = eventSite.select("h1").text()

        val startDate = try {
            parseDate(eventSite)
        } catch (exc: java.time.format.DateTimeParseException) {
            //TODO we should be able to parse this as well
            logger.info { "Error in ${fullEventLink}: can't parse date, might be a multi-day event" }
            return null
        }

        val img = eventSite.select("div.event picture img")
        val pictureUrl = if (!img.isEmpty()) {
            UrlUtils.parse(baseUrl + img.first()!!.attr("src"))
        } else {
            null
        }

        val event = structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(fullEventLink))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.field-text").text())
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(fullEventLink))
        }

        val locationName = eventSite.select("div.location").not("div.location.link-google-maps").text()
        if (locationName.isNotEmpty()) {
            val regex = """(?<name>.*?)[,|\s]*(?<zip>\d{4}) (?<city>[\w\s]+)""".toRegex()
            val matchResult = regex.find(locationName)
            if (matchResult != null) {
                modify(event) {
                    withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, matchResult.groups["name"]!!.value.trimEnd())
                    withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, matchResult.groups["city"]!!.value.trimEnd())
                }
            } else {
                modify(event) {
                    withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, locationName)
                }
            }
        }

        return event
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
