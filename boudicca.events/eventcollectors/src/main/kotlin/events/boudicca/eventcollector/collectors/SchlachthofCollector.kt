package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SchlachthofCollector : TwoStepEventCollector<Element>("schlachthof") {

    override fun getAllUnparsedEvents(): List<Element> {
        val fetcher = FetcherFactory.newFetcher()

        val document = Jsoup.parse(fetcher.fetchUrl("https://www.schlachthofwels.at/programm"))

        return document.select("div.eventitem:not(.pasteventitem)")
    }

    override fun parseStructuredEvent(event: Element): StructuredEvent {
        val name = event.select("h2").text().trim()
        val startDate = parseDate(event.select("div.event_list_details>p:nth-child(1)").text())
        val url = "https://www.schlachthofwels.at" + event.select("a.block").attr("href")
        val pictureUrl =
            "https://www.schlachthofwels.at" + parsePictureUrl(event.select("div.teaserimage").attr("style"))

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.TYPE_PROPERTY, event.select("h3:nth-child(1)").text())
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, event.select("div.event_list_previewtext").text())
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Alter Schlachthof")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.schlachthofwels.at"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wels")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
        }
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
