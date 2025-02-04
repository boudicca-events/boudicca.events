package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class FamilienkarteEventCollector : TwoStepEventCollector<String>("familienkarte") {

    // TODO: handle pagination, currently only the first 25 answers are parsed
    // TODO: handle other categories and locations (and adjust the type respectively)

    private val fetcher = Fetcher()
    private val baseUrl = "https://www.familienkarte.at"

    override fun getAllUnparsedEvents(): List<String> {
        val dateFrom = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dateTo = LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val eventsUrl =
            "$baseUrl/de/freizeit/veranstaltungen/veranstaltungskalender.html?events_cat_key=8&date_from=$dateFrom&date_to=$dateTo"
        val document = Jsoup.parse(fetcher.fetchUrl(eventsUrl))
        return document
            .select("div.detailButton a")
            .map { it.attr("href") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventUrl = baseUrl + event
        val eventSite = Jsoup.parse(fetcher.fetchUrl(eventUrl))

        val name = eventSite.select("div.eventDetailWrapper h1").text()
        val (startDate, endDate) = parseDates(eventSite)
        val pictureUrl = eventSite.select("div.eventEntry img")
            .first()
            ?.attr("src")
            ?.let { URI.create(it) }

        return StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.URL_PROPERTY, URI.create(eventUrl))
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(eventUrl))
            .withProperty(SemanticKeys.TYPE_PROPERTY, "theater")
            .withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.eventDetailDescr").text())
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, eventSite.select("div.eventDetailLocation").text())
            .build()
    }


    private fun parseDates(element: Element): Pair<OffsetDateTime, OffsetDateTime?> {
        val fullDateString = element.select("div.eventDetailWrapper").first()?.text()
            ?: throw IllegalArgumentException("Could not find element containing start date")

        val dateRegex = """\b(\d{2})\.(\d{2})\.(\d{2})\b""".toRegex()
        val dateMatch = dateRegex.find(fullDateString)
            ?: throw IllegalArgumentException("Could not find date in $fullDateString")
        val (day, month, year) = dateMatch.destructured
        val formattedDate = "$day.$month.$year"
        val localDate = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd.MM.uu"))

        val timeRegex = """\b(\d{2}:\d{2})(?:\s*-\s*(\d{2}:\d{2}))?\b""".toRegex()
        val timeMatch = timeRegex.find(fullDateString)
            ?: throw IllegalArgumentException("Could not find start (& endtime) in $fullDateString")
        val (startTimeString, endTimeString) = timeMatch.destructured
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val startTime = LocalTime.parse(startTimeString, timeFormatter)
        val endTime = endTimeString.takeIf { it.isNotEmpty() }?.let { LocalTime.parse(it, timeFormatter) }

        return Pair(
            localDate.atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
            localDate.atTime(endTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
        )
    }
}
