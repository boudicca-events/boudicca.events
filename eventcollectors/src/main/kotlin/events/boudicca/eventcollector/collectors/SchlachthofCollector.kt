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

class SchlachthofCollector : TwoStepEventCollector<Element>("schlachthof") {

    override fun getAllUnparsedEvents(): List<Element> {
        val fetcher = Fetcher()

        val document = Jsoup.parse(fetcher.fetchUrl("https://www.schlachthofwels.at/programm"))

        return document.select("div.eventitem:not(.pasteventitem)")
    }

    override fun parseEvent(event: Element): Event {
        val data = mutableMapOf<String, String>()

        val name = event.select("h2").text().trim()
        val startDate = parseDate(event.select("div.event_list_details>p:nth-child(1)").text())

        data[SemanticKeys.DESCRIPTION] = event.select("div.event_list_previewtext").text()
        data[SemanticKeys.PICTUREURL] =
            "https://www.schlachthofwels.at" + parsePictureUrl(event.select("div.teaserimage").attr("style"))
        data[SemanticKeys.URL] =
            "https://www.schlachthofwels.at" + event.select("a.block").attr("href")

        data[SemanticKeys.LOCATION_NAME] = "Alter Schlachthof"
        data[SemanticKeys.LOCATION_URL] = "https://www.schlachthofwels.at"
        data[SemanticKeys.LOCATION_CITY] = "Wels"

        return Event(name, startDate, data)
    }

    private fun parsePictureUrl(style: String): String {
        //looks like background-image:url(/uploads/_processed_/b/b/csm_0610_doomsday-clock-90-seconds-to-midnight_0fad0fee80.png)
        return style.substring(21, style.length - 1)
    }

    private fun parseDate(text: String): OffsetDateTime {
        return LocalDateTime
            .parse(text.substring(6), DateTimeFormatter.ofPattern("dd.MM.uu kk:mm"))
            .atZone(ZoneId.of("CET"))
            .toOffsetDateTime()
    }

}
