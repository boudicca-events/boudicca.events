package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class PosthofCollector : TwoStepEventCollector<String>("posthof") {

    private val fetcher = Fetcher()
    private val baseUrl = "https://www.posthof.at"

    override fun getAllUnparsedEvents(): List<String> {
        val eventUrls = mutableListOf<String>()

        var nextUrl: String = baseUrl
        while (true) {
            val currentDoc = Jsoup.parse(fetcher.fetchUrl(nextUrl))
            eventUrls.addAll(currentDoc.select("ul.programmlist li h2 a").map { baseUrl + it.attr("href") })

            val nextButton = currentDoc.select("ul.programmlist li.loadnext button")
            if (nextButton.isNotEmpty()) {
                nextUrl = baseUrl + nextButton.attr("hx-post")
            } else {
                break
            }
        }

        return eventUrls
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite: Element = Jsoup.parse(fetcher.fetchUrl(event))

        var name = eventSite.select("div.tx-posthof-events h2 a").textNodes().first().text()

        val subtextSpan = eventSite.select("div.tx-posthof-events h2 a span")
        if (subtextSpan.isNotEmpty()) {
            name += " - " + subtextSpan.text()
        }

        val dateAndTypeSpans = getDateAndTypeSpans(eventSite)
        val dateAndTimeText = getDateAndTimeText(dateAndTypeSpans)
        val startDate = LocalDateTime.parse(
            dateAndTimeText,
            DateTimeFormatter.ofPattern("dd LLL uu kk:mm", Locale.GERMAN)
        ).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()

        var description = ""
        val textBlocks = eventSite.select("div.tx-posthof-events")[1].children().drop(1)
        for (child in textBlocks) {
            if (child.tagName() == "hr") {
                break
            }
            description += child.text() + "\n"
        }

        val imgUrl = baseUrl + eventSite.select("div.tx-posthof-events img").attr("src")

        val builder = StructuredEvent.builder(name, startDate)
        mapType(builder, dateAndTypeSpans[2].text())
        return builder
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, URI.create(imgUrl))
            .withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.TICKET) //are there free events in posthof?
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Posthof")
            .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, URI.create("https://www.posthof.at"))
            .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            .withProperty(SemanticKeys.URL_PROPERTY, URI.create(event))
            .build()
    }

    private fun getDateAndTimeText(dateAndTypeSpans: Elements): String {
        return (dateAndTypeSpans[0].text() + " " + dateAndTypeSpans[1].text())
            .substring(4)
            .replace("Jän", "Jan")
    }

    private fun getDateAndTypeSpans(eventSite: Element): Elements {
        val preHeaders = eventSite.select("div.tx-posthof-events div.pre-header")
        return preHeaders.last()!!.select("span")
    }

    //TODO move to enricher
    private fun mapType(builder: StructuredEvent.StructuredEventBuilder, type: String) {
        val lowerType = type.lowercase()
        for (knownMusicType in KNOWN_MUSIC_TYPES) {
            if (lowerType.indexOf(knownMusicType) != -1) {
                builder.withProperty(SemanticKeys.TYPE_PROPERTY, "concert")
                builder.withProperty(SemanticKeys.CONCERT_GENRE_PROPERTY, type)
                return
            }
        }
        if (type.isNotBlank()) {
            builder.withProperty(SemanticKeys.TYPE_PROPERTY, type)
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
