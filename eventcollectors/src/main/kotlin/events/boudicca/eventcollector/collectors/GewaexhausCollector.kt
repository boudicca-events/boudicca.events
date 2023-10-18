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

class GewaexhausCollector : TwoStepEventCollector<String>("gewaexhaus") {
    private val fetcher = Fetcher()
    private val baseUrl = "https://www.k-plus.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "veranstaltungen/"))
        return document.select("div.ElYa0 a.VrxBN")
            .map { it.attr("href") }
    }

    override fun parseEvent(event: String): Event {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(baseUrl + event))

        val name = eventSite.select("h1").text()
        val startDate = parseDate(eventSite)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = baseUrl + event
        data[SemanticKeys.LOCATION_URL] = baseUrl
        data[SemanticKeys.LOCATION_NAME] = "Gewäxhaus Ennsdorf"
        data[SemanticKeys.LOCATION_CITY] = "Ennsdorf"

        val description = eventSite.select("section.css-9s1hn:not(section.hugeTitleFontSize)").text()
        data[SemanticKeys.DESCRIPTION] = description

        val img = eventSite.select("section.hugeTitleFontSize img")
        if (!img.isEmpty()) {
            data[SemanticKeys.PICTUREURL] = img.first()!!.attr("srcset").split(" ")[0]
        }

        return Event(name, startDate, data)
    }

    private fun parseDate(element: Element): OffsetDateTime {
        val fullDate = element.select("section.hugeTitleFontSize h3").text()
        val date = fullDate.split(", ")[1]
        val fullTime = element.select("section.hugeTitleFontSize h3 + p").text()
        val time = fullTime.split(" Uhr")[0]

        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.LL.uuuu"))
        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("kk:mm"))

        return localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
