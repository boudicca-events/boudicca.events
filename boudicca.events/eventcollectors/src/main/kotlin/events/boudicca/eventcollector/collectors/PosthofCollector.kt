package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PosthofCollector : TwoStepEventCollector<Element>("posthof") {

    override fun getAllUnparsedEvents(): List<Element> {
        val fetcher = Fetcher()
        val events = mutableListOf<Element>()

        val document = Jsoup.parse(fetcher.fetchUrl("https://www.posthof.at/programm/alles/browse/1/"))
        val otherUrls = document.select("div.news-list-browse table a")
            .toList()
            .filter {
                it.text() != "1"
            }
            .map { it.attr("href") }
        parseEventList(document, events)

        otherUrls.forEach {
            parseEventList(Jsoup.parse(fetcher.fetchUrl("https://www.posthof.at/$it")), events)
        }

        return events
    }

    private fun parseEventList(document: Document, events: MutableList<Element>) {
        events.addAll(document.select("div.event-list-item"))
    }

    override fun parseEvent(event: Element): Event {
        val data = mutableMapOf<String, String>()

        var name = event.select("div.h3>a").text()
        name += " - ${event.select("div.news-list-subtitle").text()}"
        val startDate = LocalDateTime.parse(
            event.select("span.news-list-date").text().substring(4),
            DateTimeFormatter.ofPattern("dd.MM.uuuu // kk:mm")
        ).atZone(ZoneId.of("Europe/Vienna"))

        mapType(data, event.select("span.news-list-category").text())
        data[SemanticKeys.URL] = "https://www.posthof.at/" + event.select("div.h3>a").attr("href")
        data[SemanticKeys.DESCRIPTION] = event.select("div.news_text>p").text()
        data[SemanticKeys.PICTUREURL] = "https://www.posthof.at/" + event.select("img").attr("src")

        data[SemanticKeys.REGISTRATION] = "ticket" //are there free events in posthof?
        data[SemanticKeys.LOCATION_NAME] = "Posthof"
        data[SemanticKeys.LOCATION_URL] = "https://www.posthof.at"
        data[SemanticKeys.LOCATION_CITY] = "Linz"
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

        return Event(name, startDate.toOffsetDateTime(), data)
    }

    private fun mapType(data: MutableMap<String, String>, type: String) {
        val lowerType = type.lowercase()
        for (knownMusicType in KNOWN_MUSIC_TYPES) {
            if (lowerType.indexOf(knownMusicType) != -1) {
                data[SemanticKeys.TYPE] = "concert"
                data[SemanticKeys.CONCERT_GENRE] = type
                return
            }
        }
        if (type.isNotBlank()) {
            data[SemanticKeys.TYPE] = type
        }
    }

    private val KNOWN_MUSIC_TYPES: Set<String> = setOf(
        "metal",
        "indie",
        "pop",
        "jazz",
        "hiphop",
        "rap",
        "rock",
        "electronic",
        "punk",
        "house",
        "brass",
        "reggae",
        "soul",
        "funk",
        "folk",
        "dub",
        "klassik",        //TODO moar and moar generic
    )
}
