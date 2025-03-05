package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class BrucknerhausCollector : TwoStepEventCollector<Element>("brucknerhaus") {

    override fun getAllUnparsedEvents(): List<Element> {
        val events = mutableListOf<Element>()

        val fetcher = FetcherFactory.newFetcher()
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.brucknerhaus.at/programm/veranstaltungen"))
        val otherUrls = document.select("ul.pagination>li")
            .toList()
            .filter { it.attr("class").isEmpty() }
            .map { "https://www.brucknerhaus.at" + it.children().first()!!.attr("href") }

        events.addAll(findUnparsedEvents(document))

        otherUrls.forEach {
            events.addAll(findUnparsedEvents(Jsoup.parse(fetcher.fetchUrl(it))))
        }

        return events
    }

    private fun findUnparsedEvents(doc: Document): List<Element> {
        return doc.select("div.event div.event__element")
    }

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent> {
        val startDates: List<OffsetDateTime> = parseDate(event)
        val name = event.select("div.event__name").text()
        val url = "https://www.brucknerhaus.at" + event.select("a.headline_link").attr("href")

        var description = event.select("div.event__teaser p").text()
        if (description.isBlank()) {
            description = event.select("div.event__teaser .fr-view").first()?.children()?.first()?.text() ?: ""
        }

        return startDates.map { startDate ->
            structuredEvent(name, startDate) {
                withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
                withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
                withProperty(
                    SemanticKeys.PICTURE_URL_PROPERTY,
                    UrlUtils.parse(event.select("div.event__image img").attr("src"))
                )
                withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
                withProperty(SemanticKeys.TYPE_PROPERTY, "concert") //TODO check
                withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Brucknerhaus") //TODO not all events are there...
                withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.brucknerhaus.at/"))
                withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            }
        }
    }

    private fun parseDate(event: Element): List<OffsetDateTime> {
        val dateElement = event.select("div.event__date").first()!!

        val dateText = dateElement.text().trim()
        val localDates = if (dateText.contains("-")) {
            //Sa 23 März - Do 16 Mai 24
            val split = dateText.split("-")
            val year = dateText.substring(dateText.lastIndexOf(' ') + 1)
            val startDate = parseSingleLocalDate(split[0].trim() + " " + year)
            val endDate = parseSingleLocalDate(split[1].trim())
            startDate.datesUntil(endDate.plusDays(1)).toList()
        } else {
            //So 14 Apr 24
            listOf(parseSingleLocalDate(dateText))
        }

        val timeElement = event.select("div.event__location").first()!!
        val time = timeElement.children()[0].children()[0].text()

        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("kk:mm"))

        return localDates.map { localDate ->
            localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
        }
    }

    private fun parseSingleLocalDate(dateText: String): LocalDate {
        val split = dateText.split(" ")
        return LocalDate.parse(
            split[1] + " " + mapMonth(split[2]) + " " + split[3],
            DateTimeFormatter.ofPattern("d. M uu").withLocale(Locale.GERMAN)
        )
    }

    private fun mapMonth(month: String): String {
        return when (month.uppercase()) {
            "JAN" -> "1"
            "FEB" -> "2"
            "MÄRZ" -> "3"
            "APR" -> "4"
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
