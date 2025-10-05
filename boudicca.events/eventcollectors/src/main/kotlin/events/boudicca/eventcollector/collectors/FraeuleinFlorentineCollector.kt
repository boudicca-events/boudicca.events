package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DatePair
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class FraeuleinFlorentineCollector : TwoStepEventCollector<Element>("fraeuleinflorentine") {

    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://frl-florentine.at/eventkalender/"

    override fun getAllUnparsedEvents(): List<Element> {
        return Jsoup.parse(fetcher.fetchUrl(baseUrl))
            .select("ul.simcal-events li")
            .distinctBy { it.text() } // multi-day events have the same text in multiple entries
    }

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent?> {
        val nameAndTime = event.select(".simcal-event-title").text().split("|")
        val name = nameAndTime.first().trim()
        val description = event.select(".simcal-event-description").text()
        var startDate = parseStartDate(event, nameAndTime)
        val endDate = parseEndDate(event, startDate)
        if (endDate != null) {
            startDate = DateParserResult(listOf(DatePair(startDate.dates[0].startDate, endDate.dates[0].startDate)))
        }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Salonschiff Fräulein Florentine ")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://frl-florentine.at"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
        }
    }

    private fun parseStartDate(event: Element, nameAndTime: List<String?>): DateParserResult {
        var startTimeToParse = "00:00"
        val startTimeElementText = event.select(".simcal-event-start-time").text()
        if (startTimeElementText.isNotBlank()) {
            startTimeToParse = startTimeElementText
            val startTime = LocalTime.parse( // DateParse can't handle am/pm, so convert it manually and continue with its String
                startTimeToParse.uppercase(), // convert pm to PM, to be recognized by the time pattern 'a'
                DateTimeFormatter.ofPattern("h:mm a").withLocale(Locale.GERMAN)
            )
            startTimeToParse = startTime.toString()
        } else if (nameAndTime.size > 1 && !nameAndTime[1].isNullOrBlank()) {
            startTimeToParse = nameAndTime[1]!!.trim()
        }

        val startDate = DateParser.parse(event.select(".simcal-event-start-date").text(), startTimeToParse)

        return startDate
    }

    private fun parseEndDate(event: Element, startDate: DateParserResult): DateParserResult? {
        var endTime : LocalTime? = null
        val endTimeToParse = event.select(".simcal-event-end-time").text()
        if (endTimeToParse.isNotBlank()) {
            endTime = LocalTime.parse( // DateParse can't handle am/pm, so convert it manually and continue with its String
                endTimeToParse.uppercase(), // convert pm to PM, to be recognized by the time pattern 'a'
                DateTimeFormatter.ofPattern("h:mm a").withLocale(Locale.GERMAN))
        }

        var endDate: DateParserResult? = null
        val endDateToParse = event.select(".simcal-event-end-date").text()
        if (endDateToParse.isNotBlank()) { // endDate with endDate or end of day if not time is set
            val time = endTime ?: LocalTime.MAX
            endDate = DateParser.parse(endDateToParse, time.toString())

        } else if (endTime != null) { // startDate with endTime
            endDate = DateParser.parse(startDate.dates[0].startDate.toLocalDate().toString(), endTime.toString())
        }

        return endDate
    }

}
