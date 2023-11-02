package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class StiftskonzerteCollector : TwoStepEventCollector<String>("stiftskonzerte") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.stiftskonzerte.at/programm-und-karten/"))
        return document.select("div.entry-footer-links a").not("a.open-modal")
            .map { it.attr("href") }
    }

    override fun parseEvent(event: String): Event {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))
        val name = eventSite.select("header.entry-header h1").text()

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = event
        data[SemanticKeys.DESCRIPTION] = eventSite.select("div.entry-flexible-content").text()

        val locationTimeDiv = eventSite.select("div.entry-content div.location")
        locationTimeDiv.select("br").after("\\n")
        val locationAndTime = locationTimeDiv.text()
            .split("\\n")
            .map { it.trim().replace("ü", "ü") }  // fix different ü's in Kremsmünster
            .filter { it.isNotBlank() }
        val startDate = parseDate(eventSite, locationAndTime)

        val city = locationAndTime[1].replace("Stift ", "")
        val location = locationAndTime.subList(1, locationAndTime.lastIndex + 1).joinToString(", ")
        data[SemanticKeys.LOCATION_CITY] = city
        data[SemanticKeys.LOCATION_NAME] = location

        val img = eventSite.select("div.entry-content img")
        if (!img.isEmpty()) {
            data[SemanticKeys.PICTUREURL] = img.first()!!.attr("src")
        }

        return Event(name, startDate, data)
    }

    private fun parseDate(element: Element, locationAndTime: List<String>): OffsetDateTime {
        val fullDate = element.select("div.entry-content div.date").text()
        val date = fullDate.split(", ")[1]
        val time = locationAndTime[0].replace(" Uhr", "")

        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.LL.uuuu"))
        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("kk.mm"))

        return localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
