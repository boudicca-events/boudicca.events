package events.boudicca.eventcollector.collectors

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.Fetcher
import events.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class FamilienkarteCollector : TwoStepEventCollector<String>("familienkarte") {

    private val fetcher = Fetcher()


    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.familienkarte.at/de/freizeit/veranstaltungen/veranstaltungskalender.html?events_cat_key=8"))
        return document.select("div.detailButton a")
                .map { it.attr("href") }
    }

    override fun parseEvent(event: String): Event {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("div.eventDetailWrapper h1").text()
        val (startDate, endDate) = parseDates(eventSite)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = event
        if (endDate != null) {
            data[SemanticKeys.ENDDATE] = endDate.format(DateTimeFormatter.ISO_DATE)
        }
        data[SemanticKeys.TYPE] = "other"
        data[SemanticKeys.DESCRIPTION] = eventSite.select("div.eventDetailDescr").text()

        val img = eventSite.select("div.eventEntry img")
        if (!img.isEmpty()) {
            data[SemanticKeys.PICTUREURL] = img.first()!!.attr("src")
        }

        data[SemanticKeys.LOCATION_NAME] = eventSite.select("div.eventDetailLocation").text()

        return Event(name, startDate, data)
    }


    private fun parseDates(element: Element): Pair<OffsetDateTime, OffsetDateTime?> {
        val fullDate = element.select("div.eventDetailWrapper h2").text()
        val date = fullDate.substring(3, 11)
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.uu"))

        val startAndEndTimeText = fullDate.substring(14, 27)
        val startAndEndTimes = startAndEndTimeText.split(" - ")

        val localStartTime = LocalTime.parse(startAndEndTimes[0].trim(), DateTimeFormatter.ofPattern("kk:mm"))
        val localEndTime = LocalTime.parse(startAndEndTimes[1].trim(), DateTimeFormatter.ofPattern("kk:mm"))

        return Pair(
                localDate.atTime(localStartTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
                localDate.atTime(localEndTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
        )
    }
}
