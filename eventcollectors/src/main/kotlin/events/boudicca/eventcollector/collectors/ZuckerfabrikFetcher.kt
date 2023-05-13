package events.boudicca.eventcollector.collectors

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ZuckerfabrikFetcher : TwoStepEventCollector<Pair<String, Document>>("zuckerfabrik") {

    override fun getAllUnparsedEvents(): List<Pair<String, Document>> {
        val events = mutableListOf<Pair<String, Document>>()
        val eventUrls = mutableSetOf<String>()

        val document = Jsoup.connect("https://www.zuckerfabrik.at/termine-tickets/").get()
        document.select("div#storycontent > a.bookmarklink")
            .forEach { eventUrls.add(it.attr("href")) }

        eventUrls.forEach {
            events.add(Pair(it, Jsoup.connect(it).get()))
        }

        return events
    }

    override fun parseEvent(event: Pair<String, Document>): Event? {
        val (url, doc) = event
        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = url

        var name = doc.select("div#storycontent>h2").text()

        val storycontent = doc.select("div#storycontent>p")
        if (storycontent[0].text().isNotBlank()) {
            name += " - " + storycontent[0].text()
        }
        val startDate = parseTypeAndDate(data, storycontent[1])
        data[SemanticKeys.DESCRIPTION] = (2 until storycontent.size).joinToString("\n") { storycontent[it].text() }

        data[SemanticKeys.PICTUREURL] = doc.select("div#storycontent img").attr("src")

        data[SemanticKeys.LOCATION_NAME] = "Zuckerfabrik"
        data[SemanticKeys.LOCATION_URL] = "https://www.zuckerfabrik.at"
        data[SemanticKeys.LOCATION_CITY] = "Enns"

        return Event(name, startDate, data)
    }

    private fun parseTypeAndDate(data: MutableMap<String, String>, element: Element): OffsetDateTime {
        val split = element.text().split(" am ")
        data[SemanticKeys.TYPE] = split[0]
        val dateSplit = split[1].split(",").map { it.trim() }
        val date = LocalDate.parse(
            dateSplit[1],
            DateTimeFormatter.ofPattern("d. LLLL uuuu").withLocale(Locale.GERMAN)
        )
        var startTimeString = dateSplit[2].substring(0, dateSplit[2].length - 4)
        val startTime: LocalTime
        var endTime: LocalTime? = null
        val timeFormatter = DateTimeFormatter.ofPattern("kk:mm")
        if (dateSplit[2].contains(" - ")) {
            val timeSplit = startTimeString.split(" - ")
            startTimeString = timeSplit[0]
            endTime = LocalTime.parse(timeSplit[1], timeFormatter)
        }
        startTime = LocalTime.parse(startTimeString, timeFormatter)
        val startDate = date.atTime(startTime).atZone(ZoneId.of("CET")).toOffsetDateTime()
        if (endTime != null) {
            data[SemanticKeys.ENDDATE] =
                date.atTime(startTime).atZone(ZoneId.of("CET")).toOffsetDateTime().toString()
        }
        return startDate
    }

}
