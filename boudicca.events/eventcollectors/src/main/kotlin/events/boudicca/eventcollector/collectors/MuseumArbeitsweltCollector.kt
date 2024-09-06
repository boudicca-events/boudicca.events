package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class MuseumArbeitsweltCollector : TwoStepEventCollector<Pair<String, String>>("museumArbeitswelt") {
    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<Pair<String, String>> {
        val events = mutableListOf<Pair<String, String>>()
        val document = Jsoup.parse(fetcher.fetchUrl("https://museumarbeitswelt.at/kalender/"))
        document.select("div.ecs-event").forEach() {
            events.add(Pair(
                it.select("a.act-view-more").attr("href"),
                it.select("div.decm-show-detail-center").text()
            ))
        }
        return events
    }

    override fun parseEvent(event: Pair<String, String>): Event {
        val (eventUrl, dateToParse) = event
        val eventSite = Jsoup.parse(fetcher.fetchUrl(eventUrl))

        val name = eventSite.select("h1.entry-title").text()
        val startDate = parseDate(dateToParse)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = eventUrl
        val description = eventSite.select("div.et_pb_post_content").text()
        data[SemanticKeys.DESCRIPTION] = description

        val img = eventSite.select("div.et_pb_title_featured_container span.et_pb_image_wrap img")
        if (!img.isEmpty()) {
            data[SemanticKeys.PICTURE_URL] = img.last()!!.attr("src")
        }

        data[SemanticKeys.LOCATION_NAME] = "Museum Arbeitswelt"
        data[SemanticKeys.LOCATION_URL] = "https://museumarbeitswelt.at/"
        data[SemanticKeys.LOCATION_CITY] = "Steyr"
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

        return Event(name, startDate, data)
    }

    private fun parseDate(dateToParse: String): OffsetDateTime {
        val localDateTime = LocalDateTime.parse(dateToParse.replace("All Day Event","00.00"),
            DateTimeFormatter.ofPattern("d. MMMM uuuu k.mm", Locale.GERMAN))
        return localDateTime.atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
