package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Event
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
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

        data[base.boudicca.SemanticKeys.TYPE] = event.select("h3:nth-child(1)").text()
        data[base.boudicca.SemanticKeys.DESCRIPTION] = event.select("div.event_list_previewtext").text()
        data[base.boudicca.SemanticKeys.PICTUREURL] =
            "https://www.schlachthofwels.at" + parsePictureUrl(event.select("div.teaserimage").attr("style"))
        data[base.boudicca.SemanticKeys.URL] =
            "https://www.schlachthofwels.at" + event.select("a.block").attr("href")

        data[base.boudicca.SemanticKeys.LOCATION_NAME] = "Alter Schlachthof"
        data[base.boudicca.SemanticKeys.LOCATION_URL] = "https://www.schlachthofwels.at"
        data[base.boudicca.SemanticKeys.LOCATION_CITY] = "Wels"
        data[base.boudicca.SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY] = "true"
        data[base.boudicca.SemanticKeys.ACCESSIBILITY_ACCESSIBLESEATS] = "true"
        data[base.boudicca.SemanticKeys.ACCESSIBILITY_ACCESSIBLETOILETS] = "true"

        return Event(name, startDate, data)
    }

    private fun parsePictureUrl(style: String): String {
        //looks like background-image:url(/uploads/_processed_/b/b/csm_0610_doomsday-clock-90-seconds-to-midnight_0fad0fee80.png)
        return style.substring(21, style.length - 1)
    }

    private fun parseDate(text: String): OffsetDateTime {
        return LocalDateTime
            .parse(text.substring(6), DateTimeFormatter.ofPattern("dd.MM.uu kk:mm"))
            .atZone(ZoneId.of("Europe/Vienna"))
            .toOffsetDateTime()
    }

}
