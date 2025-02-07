package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class StadthalleWienCollector : TwoStepEventCollector<String>("stadthallewien") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.stadthalle.com"

    override fun getAllUnparsedEvents(): List<String> {

        val document = Jsoup.parse(fetcher.fetchUrl("https://www.stadthalle.com/de/events/alle-events"))
        return document
            .select("div.event-item-inner a.front-side")
            .map { baseUrl + it.attr("href") }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("h1.title").text()


        val srcAttr = eventSite.select("div.img-ad img").attr("src")
        val pictureUrl = if (srcAttr.isNotBlank()) {
            UrlUtils.parse(baseUrl + srcAttr)
        } else {
            null
        }

        val startDates = parseDates(eventSite) //we could parse enddates but this is kinda tricky

        val builder = StructuredEvent
            .builder()
            .withName(name)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.readmore-txt").text())
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Stadthalle Wien")
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))

        return startDates.map {
            builder
                .copy()
                .withStartDate(it)
                .build()
        }

    }

    private fun parseDates(eventSite: Element): Set<OffsetDateTime> {
        return eventSite.select("ul#datetable li")
            .map {
                //this is amazing, thank you stadthalle (... or it would be if it actually would be available everywhere -.-
                val startDateSelector = it.select("meta[itemprop=startDate]")
                val startDate = if (startDateSelector.attr("content").isNotBlank()) {
                    OffsetDateTime.parse(
                        startDateSelector.attr("content"),
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                } else {
                    val dateText = it.select("h3:nth-child(1)").text()
                    val timeText = it.select("h3:nth-child(2)").text()

                    val date = LocalDate.parse(
                        dateText,
                        DateTimeFormatter.ofPattern("dd.MM.uuuu").withLocale(Locale.GERMAN)
                    )
                    val time = LocalTime.parse(
                        timeText,
                        DateTimeFormatter.ofPattern("kk:mm 'Uhr'").withLocale(Locale.GERMAN)
                    )

                    date.atTime(time).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
                }
                val endDateSelector = it.select("meta[itemprop=endDate]")
                if (endDateSelector.isNotEmpty()) {
                    //an enddate here seems to mean daily?
                    val endDate = OffsetDateTime.parse(
                        endDateSelector.attr("content"),
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                    val allDates = mutableListOf<OffsetDateTime>()
                    var currentDate = startDate
                    while (currentDate <= endDate) {
                        currentDate = currentDate.plusDays(1)
                        allDates.add(currentDate)
                    }
                    allDates
                } else {
                    listOf(startDate)
                }
            }
            .flatten()
            .toSet()
    }

}
