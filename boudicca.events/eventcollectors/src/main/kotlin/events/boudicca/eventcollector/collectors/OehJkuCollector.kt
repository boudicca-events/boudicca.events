package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["oehjku"])
class OehJkuCollector : TwoStepEventCollector<String>("oehjku") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://oeh.jku.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "oeh-services/veranstaltungen"))
        return document.select("article.card a").map { it.attr("href") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(baseUrl + event))

        val name = eventSite.select("section div.container h1").text()
        val startDate = parseDate(eventSite)

        val location = findTextByIconHref(eventSite, "icon-pin")

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(baseUrl, event))
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, location)
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.jku.at/"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl + event))
        }
    }

    private fun parseDate(eventSite: Element): OffsetDateTime {
        val date = findTextByIconHref(eventSite, "icon-calendar")
        val time = findTextByIconHref(eventSite, "icon-time")

        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("d.M.uuuu", Locale.GERMAN))
        val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("k:mm"))

        return localDate.atTime(localTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

    private fun findTextByIconHref(
        eventSite: Element,
        iconHref: String,
    ): String {
        val icon = eventSite.select("div.teaser div.list-group-item svg use[xlink:href='#$iconHref']")[0]
        return icon.parent()?.parent()?.text() ?: error("icon '$iconHref' not found")
    }
}
