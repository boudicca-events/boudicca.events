package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.annotations.BoudiccaEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup

@BoudiccaEventCollector("spinnerei")
class SpinnereiCollector : TwoStepEventCollector<String>("spinnerei") {
    private val baseUrl = "https://spinnerei.kulturpark.at"
    val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val events = mutableListOf<String>()
        val eventUrls = mutableSetOf<String>()

        val document = Jsoup.parse(fetcher.fetchUrl("$baseUrl/programm/"))
        document
            .select("div.container-programm-uebersicht > a")
            .forEach { eventUrls.add(it.attr("href")) }

        eventUrls.forEach {
            events.add(it)
        }

        return events
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent> {
        val doc = Jsoup.parse(fetcher.fetchUrl(event))

        var name = doc.select("div.vng-details div.vng-detail-content-titel").text()
        name += " " + doc.select("div.vng-details div.vng-detail-content-untertitel").text()

        val (startDate, type) = parseTypeAndDate(doc.select("div.vng-details div.vng-detail-content-beginn").text())
        val picture = doc.select("div.vng-details div.bg-image").first()
        val pictureUrl =
            if (picture != null) {
                baseUrl + parsePictureUrl(picture.attr("style"))
            } else {
                null
            }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, doc.select("div.vng-detail-content-bodytext").text())
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Spinnerei")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Spinnerei")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Traun")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
        }
    }

    private fun parsePictureUrl(style: String): String {
        // looks like background-image:url('/media/47704/c-wolf-gruber_hypnotica_pr-foto.png?center=0.39316239316239315,0.22666666666666666&mode=crop&width=320&height=180');
        return style.substring(22, style.length - 3)
    }

    private fun parseTypeAndDate(text: String): Pair<DateParserResult, String> {
        val split = text.split(' ', ignoreCase = false, limit = 4)
        check(split.size == 4) { "could not parse type and date from $text" }

        return Pair(DateParser.parse(text), split[3])
    }
}
