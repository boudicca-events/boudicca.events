package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ZuckerfabrikCollector : TwoStepEventCollector<Pair<String, Document>>("zuckerfabrik") {

    override fun getAllUnparsedEvents(): List<Pair<String, Document>> {
        val fetcher = Fetcher()
        val events = mutableListOf<Pair<String, Document>>()
        val eventUrls = mutableSetOf<String>()

        val document = Jsoup.parse(fetcher.fetchUrl("https://www.zuckerfabrik.at/termine-tickets/"))
        document.select("div#storycontent > a.bookmarklink")
            .forEach { eventUrls.add(it.attr("href")) }

        eventUrls.forEach {
            events.add(Pair(it, Jsoup.parse(fetcher.fetchUrl(it))))
        }

        return events
    }

    override fun parseEvent(event: Pair<String, Document>): Event {
        val (url, doc) = event
        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = url

        var name = doc.select("div#storycontent>h2").text()

        val storycontent = doc.select("div#storycontent>p")
        if (storycontent[0].text().isNotBlank()) {
            name += " - " + storycontent[0].text()
        }
        val dateIndex = findDateIndex(storycontent)
        val startDate = parseTypeAndDate(data, storycontent[dateIndex])
        data[SemanticKeys.DESCRIPTION] =
            ((dateIndex + 1) until storycontent.size).joinToString("\n") { storycontent[it].text() }

        val pictureUrl = doc.select("div#storycontent img").attr("src")
        if (pictureUrl.isNotBlank()) {
            data[SemanticKeys.PICTUREURL] = pictureUrl
        }

        data[SemanticKeys.LOCATION_NAME] = "Zuckerfabrik"
        data[SemanticKeys.LOCATION_URL] = "https://www.zuckerfabrik.at"
        data[SemanticKeys.LOCATION_CITY] = "Enns"
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

        return Event(name, startDate, data)
    }

    private fun findDateIndex(storycontent: Elements): Int {
        for (i in 1 until storycontent.size) {
            if (storycontent[i].text().contains(" am ")) {
                return i
            }
        }
        throw IllegalStateException("could not find date index in: $storycontent")
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
            endTime = LocalTime.parse(timeSplit[1].replace('.', ':'), timeFormatter)
        }
        if (startTimeString.endsWith(" Uhr")) {
            startTimeString = startTimeString.substring(0, startTimeString.length - 4)
        }
        startTime = LocalTime.parse(startTimeString.replace('.', ':'), timeFormatter)
        val startDate = date.atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
        if (endTime != null) {
            data[SemanticKeys.ENDDATE] =
                DateTimeFormatter.ISO_DATE_TIME.format(
                    date.atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
                )
        }
        return startDate
    }

}
