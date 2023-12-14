package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.*
import java.time.format.DateTimeFormatter

class OKHVoecklabruckCollector : TwoStepEventCollector<Pair<String, String>>("okhvoecklabruck") {
    private val fetcher = Fetcher()
    private val baseUrl = "https://www.okh.or.at/"

    override fun getAllUnparsedEvents(): List<Pair<String, String>> {
        val events = mutableListOf<Pair<String, String>>()
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "programm"))
        document.select("div.event_box").forEach {
            events.add(Pair(
                it.select("a.event-inner").attr("href"),
                it.select("div.event-art").text()
            ))
        }
        return events
    }

    override fun parseEvent(event: Pair<String, String>): Event {
        val (eventUrl, eventType) = event
        val url = baseUrl + eventUrl
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1").text()
        val startDate = parseDate(eventSite)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = url
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

        if(eventType.isNotBlank()){
            data[SemanticKeys.TYPE] = eventType
        }

        data[SemanticKeys.DESCRIPTION] = eventSite.select("div.box_3").text()
        data[SemanticKeys.PICTUREURL] = baseUrl + eventSite.select("div#headerpic img.header").attr("src").trim()

        data[SemanticKeys.LOCATION_NAME] = "Offenes Kulturhaus Vöcklabruck"
        data[SemanticKeys.LOCATION_URL] = baseUrl
        data[SemanticKeys.LOCATION_CITY] = "Vöcklabruck"

        val locationInfo = eventSite.select("p.ort").text()
        if(locationInfo.contains("Barrierefreier Zugang")){
            data[SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY] = "true"
        }

        return Event(name, startDate, data)
    }

    private fun parseDate(element: Element): OffsetDateTime {
        val fullDateTime = element.select("div.box_datum p")
        val date = fullDateTime[0].text()
        val time = fullDateTime[1].text().split("Beginn: ")[1].split(" Uhr")[0]
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.uu"))
        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("kk:mm"))
        return localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
