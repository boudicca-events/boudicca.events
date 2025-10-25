package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class SchlachthofCollector : TwoStepEventCollector<Element>("schlachthof") {
    private val baseUrl = "https://www.schlachthofwels.at"

    override fun getAllUnparsedEvents(): List<Element> {
        val fetcher = FetcherFactory.newFetcher()

        val document = Jsoup.parse(fetcher.fetchUrl("$baseUrl/programm"))

        return document.select("div.eventitem:not(.pasteventitem)")
    }

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent> {
        val name = event.select("h2").text().trim()
        val startDate = DateParser.parse(event.select("div.event_list_details>p:nth-child(1)").text())
        val url = baseUrl + event.select("a.block").attr("href")
        val pictureUrl = baseUrl + parsePictureUrl(event.select("div.teaserimage").attr("style"))
        val tags = event.select(".eventitem h3").map { it.text() }.flatMap { it.split(Regex("""[@/]""")) }

        var pictureCopyright = "Alter Schlachthof"
        if (pictureUrl.contains("_c_")) { // looks like imageName_c_copyright_info.png
            pictureCopyright = pictureUrl.split("_c_")[1].split(".")[0].replace("_", " ")
        }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.TYPE_PROPERTY, event.select("h3:nth-child(1)").text())
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, event.select("div.event_list_previewtext").text())
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, pictureCopyright)
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Alter Schlachthof")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wels")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            if (tags.isNotEmpty()) withProperty(SemanticKeys.TAGS_PROPERTY, tags)
        }
    }

    private fun parsePictureUrl(style: String): String {
        // looks like background-image:url(/uploads/_processed_/b/b/csm_0610_doomsday-clock-90-seconds-to-midnight_0fad0fee80.png)
        return style.substring(21, style.length - 1)
    }
}
