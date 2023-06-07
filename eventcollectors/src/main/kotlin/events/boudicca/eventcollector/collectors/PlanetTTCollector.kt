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

class PlanetTTCollector : TwoStepEventCollector<Element>("planettt") {

    override fun getAllUnparsedEvents(): List<Element> {
        val fetcher = Fetcher()
        val document = Jsoup.parse(fetcher.fetchUrl("https://planet.tt/index.php?article_id=173&start=0&limit=200&locat=Home"))
        return document.select("article.event_articles")
    }

    override fun parseEvent(event: Element): Event {
        val startDate = parseDate(event)
        val data = mutableMapOf<String, String>()

        val name = event.select("div.eventkasten h6").text()
        data[SemanticKeys.URL] = "https://planet.tt/index.php?article_id=148&va=" + event.attr("id")
        data[SemanticKeys.PICTUREURL] =
            "https://planet.tt/" + event.select("div.eventkasten>div:nth-child(2)>img").attr("src")
        data[SemanticKeys.DESCRIPTION] = event.select("div.eventkasten>div.completeInfo")
            .flatMap { it.children() }
            .dropLast(2)
            .joinToString("\n") { it.text() }
            .trim()

        data[SemanticKeys.TYPE] = "concert"
        mapLocation(data, event)

        return Event(name, startDate, data)
    }

    private fun mapLocation(data: MutableMap<String, String>, event: Element) {
        val location = event.select("header:nth-child(1) img").attr("src")
        if (location.contains("simmcity")) {
            data[SemanticKeys.LOCATION_NAME] = "SiMMCity"
            data[SemanticKeys.LOCATION_URL] = "https://simmcity.at/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else if (location.contains("szene")) {
            data[SemanticKeys.LOCATION_NAME] = "Szene"
            data[SemanticKeys.LOCATION_URL] = "https://szene.wien/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else if (location.contains("planet")) {
            data[SemanticKeys.LOCATION_NAME] = "Gasometer"
            data[SemanticKeys.LOCATION_URL] = "https://www.gasometer.at/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else {
            println("could not guess location from img src: $location")
        }
    }

    private fun parseDate(event: Element): OffsetDateTime {
        val firstPart = event.select("header>div:nth-child(1) p:nth-child(4)").text()
        val time = event.select("header>div:nth-child(1) p:nth-child(6)").text()
        val date = ("$firstPart $time").replace("'", "")

        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.uu kk:mm'Uhr'")).atZone(ZoneId.of("CET"))
            .toOffsetDateTime()
    }

}
