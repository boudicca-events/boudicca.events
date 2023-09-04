package events.boudicca.eventcollector.collectors

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.Fetcher
import events.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TribueneLinzCollector : TwoStepEventCollector<String>("tribünelinz") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {

        val document = Jsoup.parse(fetcher.fetchUrl("https://www.tribuene-linz.at/spielplan"))

        return document.select("div[role=listitem]")
            .map {
                var href = it.select("a:contains(Zum Stück)").attr("href")
                if (href == "") {
                    it.select("a:contains(Zum Abend)").attr("href")
                }
                if (href == "") {
                    it.select("a:contains(Zum Konzert)").attr("href")
                }
                href
            }
            .distinct()
    }

    override fun parseMultipleEvents(event: String): List<Event> {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val dates = parseDates(eventSite)

        return dates.map {
            val data = mutableMapOf<String, String>()
            val name = eventSite.select("h2[style=font-size:50px;]").text()

            data[SemanticKeys.URL] = event
            data[SemanticKeys.DESCRIPTION] = eventSite.select("h2[style=font-size:50px;]").text()
            data[SemanticKeys.PICTUREURL] = "https://www.posthof.at/" + eventSite.select("img").attr("src")

            data[SemanticKeys.REGISTRATION] = "ticket"
            data[SemanticKeys.LOCATION_NAME] = "Tribüne Linz"
            data[SemanticKeys.LOCATION_URL] = "https://www.tribuene-linz.at"
            data[SemanticKeys.LOCATION_CITY] = "Linz"
            Event(name, it, data)
        }
    }

    private fun parseDates(event: Element): List<OffsetDateTime> {

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
        data[SemanticKeys.TYPE] = type
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
