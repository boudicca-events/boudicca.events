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
import java.util.*

class KapuCollector : TwoStepEventCollector<String>("kapu") {

    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.kapu.or.at/events"))
        return document.select("article.event")
            .map { it.attr("about") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val url = "https://www.kapu.or.at$event"
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1").text()
        val startDate = parseDate(eventSite)

        var description = eventSite.select("div.textbereich__field-text").text()
        if (description.isBlank()) {
            description = eventSite.select("div.text-bild__field-image-text").text()
        }

        val imgSrc = eventSite.select("article.event img.media__element").attr("data-src")
        val pictureUrl = if (imgSrc.isNotBlank()) {
            "https://www.kapu.or.at$imgSrc"
        } else {
            null
        }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(
                SemanticKeys.TYPE_PROPERTY,
                eventSite.select("article.event > div.container > div.wot").text()
            )
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                if (pictureUrl != null) UrlUtils.parse(pictureUrl) else null
            )
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Kapu")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.kapu.or.at"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
        }
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
