package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class FraeuleinFlorentineCollector : TwoStepEventCollector<Element>("fraeuleinflorentine") {

    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://frl-florentine.at/eventkalender/"

    override fun getAllUnparsedEvents(): List<Element> {
        return Jsoup.parse(fetcher.fetchUrl(baseUrl))
            .select("ul.simcal-events li")
            .distinctBy { it.text() } // multi-day events have the same text in multiple entries
    }

    override fun parseStructuredEvent(event: Element): StructuredEvent {
        val nameAndTime = event.select(".simcal-event-title").text().split("|")
        val name = nameAndTime.first().trim()
        val description = event.select(".simcal-event-description").text()
        val startDate = parseStartDate(event, nameAndTime)
        val endDate = parseEndDate(event, startDate)

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Salonschiff Fräulein Florentine ")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://frl-florentine.at"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
        }
    }

    private fun parseStartDate(event: Element, nameAndTime: List<String?>): OffsetDateTime {
        var startTimeToParse = "00:00 AM"
        var timePattern = "h:mm a"
        val startTimeElementText = event.select(".simcal-event-start-time").text()
        if (startTimeElementText.isNotBlank()) {
            startTimeToParse = startTimeElementText
        } else if (nameAndTime.size > 1 && !nameAndTime[1].isNullOrBlank()) {
            startTimeToParse = nameAndTime[1]!!.trim()
            timePattern = "H:mm 'UHR'"
        }
        val startTime = LocalTime.parse(
            startTimeToParse.uppercase(), // convert pm to PM, to be recognized by the time pattern 'a'
            DateTimeFormatter.ofPattern(timePattern).withLocale(Locale.GERMAN)
        )

        val startDate = LocalDate.parse(
            event.select(".simcal-event-start-date").text(),
            DateTimeFormatter.ofPattern("MMMM d, yyyy").withLocale(Locale.ENGLISH)
        ).atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()

        return startDate
    }

    private fun parseEndDate(event: Element, startDate: OffsetDateTime): OffsetDateTime? {
        var endTime : LocalTime? = null
        val endTimeToParse = event.select(".simcal-event-end-time").text()
        if (endTimeToParse.isNotBlank()) {
            endTime = LocalTime.parse(
                endTimeToParse.uppercase(), // convert pm to PM, to be recognized by the time pattern 'a'
                DateTimeFormatter.ofPattern("h:mm a").withLocale(Locale.GERMAN))
        }

        var endDate: OffsetDateTime? = null
        val endDateToParse = event.select(".simcal-event-end-date").text()
        if (endDateToParse.isNotBlank()) { // endDate with endDate or end of day if not time is set
            val time = endTime ?: LocalTime.MAX
            endDate = LocalDate.parse(
                endDateToParse,
                DateTimeFormatter.ofPattern("MMMM d, yyyy").withLocale(Locale.ENGLISH)
            ).atTime(time).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()

        } else if (endTime != null) { // startDate with endTime
            endDate = startDate.toLocalDate()
                .atTime(endTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
        }

        return endDate
    }

}
