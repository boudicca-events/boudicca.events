package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.TextProperty
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.util.concurrent.TimeUnit

class AlpenvereinCollector : TwoStepEventCollector<String>("alpenverein") {
    private val delay: Long = TimeUnit.SECONDS.toMillis(12) // they request a crawl-delay of 12 seconds
    private val fetcher = FetcherFactory.newFetcher(manualSetDelay = delay)

    override fun getAllUnparsedEvents(): List<String> {
        val mainSearchPage = Jsoup.parse(fetcher.fetchUrl("https://www.alpenverein.at/portal/termine/suche.php"))

        val allSearchPages = mainSearchPage.select("div.pager a")
            .map { normalizeUrl(it.attr("href")) }
            .distinct()
            .filter { !it.endsWith("=1") }
            .map { Jsoup.parse(fetcher.fetchUrl(it)) }
            .plus(mainSearchPage)

        val allLinks = allSearchPages.flatMap { doc ->
            doc.select("div.elementList h2.listEntryTitle a")
                .map { normalizeUrl(it.attr("href")) }
        }.distinct()

        return allLinks
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val uri = URI.create(event)
        if (uri.host.contains("alpenverein-edelweiss")) { //TODO fix those
            return emptyList()
        }
        if (uri.host.contains("programm.alpenverein.wien")) { //TODO fix those
            return emptyList()
        }

        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        if (eventSite
                .select("div.blockContentInner").text()
                .contains("Die Detailansicht der Veranstaltung kann nicht gefunden werden!")
        ) {
            return emptyList()
        }

        val name = eventSite.select("div.elementBoxSheet h2").text()
        val (imgUrl, imgAltText) = getPictureUrlAndAltText(eventSite)

        return structuredEvent(name, parseDates(eventSite)) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.SPORT)
            withProperty(SemanticKeys.TYPE_PROPERTY, "sport")
            withProperty(TextProperty("sport.participation"), "active")
            withProperty(
                SemanticKeys.DESCRIPTION_TEXT_PROPERTY,
                eventSite.select("div.elementBoxSheet div.elementText").text()
            )
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, getLocationCity(eventSite))
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, imgUrl)
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, imgAltText)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Alpenverein")
        }
    }

    private fun getLocationCity(eventSite: Document): String? {
        val locationElements = eventSite.select("div.elementBoxSheet dl#officeveranstaltung_ort1")
        if (locationElements.isNotEmpty()) {
            return locationElements.text().removePrefix("Ort: ")
        }
        return null
    }

    private fun getPictureUrlAndAltText(eventSite: Document): Pair<URI?, String?> {
        var imageElements = eventSite.select("div.elementBoxSheet dl#officeveranstaltung_bild1 img")
        if (imageElements.isEmpty() || imageElements.attr("src").isBlank()) {
            imageElements = eventSite.select("a#logo img")
        }
        if (imageElements.isNotEmpty()) {
            val pictureUrl = imageElements.attr("src")
            val altText = imageElements.attr("alt")
            if (pictureUrl.isNotBlank()) {
                return Pair(UrlUtils.parse(normalizeUrl(pictureUrl)), altText)
            }
        }
        return Pair(null, null)
    }

    private fun parseDates(eventSite: Document): DateParserResult {
        val times = eventSite.select("div.elementBoxSheet dl#officeveranstaltung_zeitraum1 h3")
        val timeText = if (times.isNotEmpty()) {
            times.text()
        } else {
            eventSite.select("div.elementBoxSheet div.elementText > p.subline").text()
        }
        return DateParser.parse(timeText)
    }

    private fun normalizeUrl(url: String): String {
        val secureUrl = url.replace("http://", "https://")
        return if (secureUrl.startsWith("https://")) {
            secureUrl
        } else {
            "https://www.alpenverein.at$secureUrl"
        }
    }
}
