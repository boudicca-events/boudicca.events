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
import java.util.*

class KapuCollector : TwoStepEventCollector<String>("kapu") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.kapu.or.at/events"))
        return document.select("article.event")
            .map { it.attr("about") }
    }

    override fun parseEvent(event: String): Event {
        val url = "https://www.kapu.or.at$event"
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1").text()
        val startDate = parseDate(eventSite)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = url
        data[SemanticKeys.TYPE] = eventSite.select("article.event > div.container > div.wot").text()
        data[SemanticKeys.DESCRIPTION] = eventSite.select("div.textbereich__field-text").text()
        data[SemanticKeys.PICTUREURL] = "https://www.kapu.or.at" + eventSite.select("article.event img.media__image").attr("data-src")

        data[SemanticKeys.LOCATION_NAME] = "Kapu"
        data[SemanticKeys.LOCATION_URL] = "https://www.kapu.or.at"
        data[SemanticKeys.LOCATION_CITY] = "Linz"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY] = "true"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLESEATS] = "true"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLETOILETS] = "true"

        return Event(name, startDate, data)
    }

    private fun parseDate(element: Element): OffsetDateTime {
        val fullDateTime = element.select("article.event > div.container div.wob:nth-child(1)").text()

        val split = fullDateTime.split(". ", ignoreCase = true, limit = 2)
        val dateTime = split[1]

        return LocalDateTime.parse(
            dateTime,
            DateTimeFormatter.ofPattern("dd.LL.uuuu - kk:mm").withLocale(Locale.GERMAN)
        ).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
