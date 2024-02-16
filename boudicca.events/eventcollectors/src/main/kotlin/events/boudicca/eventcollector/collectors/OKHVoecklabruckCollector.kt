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

class OKHVoecklabruckCollector : TwoStepEventCollector<Pair<String, String>>("okhvoecklabruck") {
    private val fetcher = Fetcher()
    private val baseUrl = "https://www.okh.or.at/"

    override fun getAllUnparsedEvents(): List<Pair<String, String>> {
        val events = mutableListOf<Pair<String, String>>()
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "programm"))
        document.select("div.event_box").forEach {
            events.add(
                Pair(
                    it.select("a.event-inner").attr("href"),
                    it.select("div.event-art").text()
                )
            )
        }
        return events
    }

    override fun parseEvent(event: Pair<String, String>): Event {
        val (eventUrl, eventType) = event
        val url = baseUrl + eventUrl
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1").text()
        val dates = parseDates(eventSite)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = url
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

        if (eventType.isNotBlank()) {
            data[SemanticKeys.TYPE] = eventType
        }

        data[SemanticKeys.DESCRIPTION] = eventSite.select("div.box_3").text()
        data[SemanticKeys.PICTUREURL] = baseUrl + eventSite.select("div#headerpic img.header").attr("src").trim()

        data[SemanticKeys.LOCATION_NAME] = "Offenes Kulturhaus Vöcklabruck"
        data[SemanticKeys.LOCATION_URL] = baseUrl
        data[SemanticKeys.LOCATION_CITY] = "Vöcklabruck"

        val locationInfo = eventSite.select("p.ort").text()
        if (locationInfo.contains("Barrierefreier Zugang")) {
            data[SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY] = "true"
        }
        if (dates.second != null) {
            data[SemanticKeys.ENDDATE] = DateTimeFormatter.ISO_DATE_TIME.format(dates.second)
        }

        return Event(name, dates.first, data)
    }

    private fun parseDates(element: Element): Pair<OffsetDateTime, OffsetDateTime?> {
        val fullDateTime = element.select("div.box_datum p")

        val dateText = fullDateTime[0].text()

        val (startDate, endDate) =
            if (dateText.contains("-")) {
                //start from: 09.-11.05.24
                //["09.", "11.05.24"]
                val split = dateText.split("-").map { it.trim() }
                //["11", "05.24"]
                val secondSplit = split[1].split(".", limit = 2)

                val dayStart = split[0].substring(0, split[0].length - 1)
                val dayEnd = secondSplit[0]
                Pair(
                    LocalDate.parse(
                        "${dayStart}.${secondSplit[1]}",
                        DateTimeFormatter.ofPattern("dd.MM.uu")
                    ),
                    LocalDate.parse(
                        "${dayEnd}.${secondSplit[1]}",
                        DateTimeFormatter.ofPattern("dd.MM.uu")
                    ),
                )
            } else {
                Pair(LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd.MM.uu")), null)
            }


        val time = if (fullDateTime.size > 1 && fullDateTime[1].text().isNotBlank()) fullDateTime[1].text()
            .split("Beginn: ")[1].split(" Uhr")[0] else "00:00"
        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("kk:mm"))


        return Pair(
            startDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
            endDate?.atTime(localTime)?.atZone(ZoneId.of("Europe/Vienna"))?.toOffsetDateTime()
        )
    }

}
