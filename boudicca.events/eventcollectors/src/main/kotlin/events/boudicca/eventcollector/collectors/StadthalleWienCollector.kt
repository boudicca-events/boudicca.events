package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class StadthalleWienCollector : TwoStepEventCollector<String>("stadthallewien") {
    private val fetcher = Fetcher()
    private val baseUrl = "https://www.stadthalle.com"

    override fun getAllUnparsedEvents(): List<String> {

        val document = Jsoup.parse(fetcher.fetchUrl("https://www.stadthalle.com/de/events/alle-events"))
        return document
            .select("div.event-item-inner a.front-side")
            .map { baseUrl + it.attr("href") }
    }

    override fun parseMultipleEvents(event: String): List<Event?>? {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("h1.title").text()

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = event

        val pictureUrl = eventSite.select("div.img-ad img").attr("src")
        if (pictureUrl.isNotBlank()) {
            data[SemanticKeys.PICTUREURL] = baseUrl + pictureUrl
        }

        data[SemanticKeys.DESCRIPTION] = eventSite.select("div.readmore-txt").text()

        data[SemanticKeys.LOCATION_NAME] = "Stadthalle Wien"
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

        val startDates = parseDates(eventSite) //we could parse enddates but this is kinda tricky

        return startDates.map { Event(name, it, data.toMap()) }

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
