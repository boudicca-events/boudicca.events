package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.URI
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["familienkarte"])
class FamilienkarteEventCollector : TwoStepEventCollector<String>("familienkarte") {
    // TODO: handle pagination, currently only the first 25 answers are parsed
    // TODO: handle other categories and locations (and adjust the type respectively)

    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.familienkarte.at"

    override fun getAllUnparsedEvents(): List<String> {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateFrom = LocalDate.now().format(dateTimeFormatter)
        val dateTo = LocalDate.now().plusYears(1).format(dateTimeFormatter)

        @Suppress("MaxLineLength") // this is an url that would not gain readability by linebreaking
        val eventsUrl =
            "$baseUrl/de/freizeit/veranstaltungen/veranstaltungskalender.html?events_cat_key=8&date_from=$dateFrom&date_to=$dateTo"
        val document = Jsoup.parse(fetcher.fetchUrl(eventsUrl))
        return document.select("div.detailButton a").map { it.attr("href") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventUrl = baseUrl + event
        val eventSite = Jsoup.parse(fetcher.fetchUrl(eventUrl))

        val name = eventSite.select("div.eventDetailWrapper h1").text()
        val (startDate, endDate) = parseStartAndEndDateTime(eventSite)
        val pictureUrl =
            eventSite
                .select("div.eventEntry img")
                .first()
                ?.attr("src")
                ?.let { URI.create(it) }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, URI.create(eventUrl))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(eventUrl))
            withProperty(SemanticKeys.TYPE_PROPERTY, "theater")
            withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.eventDetailDescr").text())
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, eventSite.select("div.eventDetailLocation").text())
        }
    }

    private fun parseStartAndEndDateTime(element: Element): Pair<OffsetDateTime, OffsetDateTime?> {
        val fullDateString =
            element.select("div.eventDetailWrapper").first()?.text() ?: throw IllegalArgumentException(
                "Could not find element containing start date",
            )

        val localDate = parseDate(fullDateString)
        val (startTime, endTime) = parseStartAndEndtime(fullDateString)

        return Pair(
            localDate.atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
            localDate.atTime(endTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
        )
    }

    private fun parseDate(fullDateString: String): LocalDate {
        val dateRegex = """\b(\d{2})\.(\d{2})\.(\d{2})\b""".toRegex()
        val dateMatch =
            dateRegex.find(fullDateString) ?: throw IllegalArgumentException("Could not find date in $fullDateString")
        val (day, month, year) = dateMatch.destructured

        val formattedDate = "$day.$month.$year"
        val localDate = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd.MM.uu"))

        return localDate
    }

    private fun parseStartAndEndtime(fullDateString: String): Pair<LocalTime?, LocalTime?> {
        val timeRegex = """\b(\d{2}:\d{2})(?:\s*-\s*(\d{2}:\d{2}))?\b""".toRegex()
        val timeMatch =
            timeRegex.find(fullDateString)
                ?: throw IllegalArgumentException("Could not find start (& endtime) in $fullDateString")
        val (startTimeString, endTimeString) = timeMatch.destructured

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val startTime = LocalTime.parse(startTimeString, timeFormatter)
        val endTime = endTimeString.takeIf { it.isNotEmpty() }?.let { LocalTime.parse(it, timeFormatter) }

        return Pair(startTime, endTime)
    }
}
