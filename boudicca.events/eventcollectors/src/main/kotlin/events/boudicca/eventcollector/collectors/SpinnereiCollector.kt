package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SpinnereiCollector : TwoStepEventCollector<Pair<String, Document>>("spinnerei") {

    override fun getAllUnparsedEvents(): List<Pair<String, Document>> {
        val fetcher = FetcherFactory.newFetcher()
        val events = mutableListOf<Pair<String, Document>>()
        val eventUrls = mutableSetOf<String>()

        val document = Jsoup.parse(fetcher.fetchUrl("https://spinnerei.kulturpark.at/programm/"))
        document.select("div.container-programm-uebersicht > a")
            .forEach { eventUrls.add(it.attr("href")) }

        eventUrls.forEach {
            events.add(Pair(it, Jsoup.parse(fetcher.fetchUrl(it))))
        }

        return events
    }

    override fun parseStructuredEvent(event: Pair<String, Document>): StructuredEvent {
        val (url, doc) = event

        var name = doc.select("div.vng-details div.vng-detail-content-titel").text()
        name += " " + doc.select("div.vng-details div.vng-detail-content-untertitel").text()

        val (startDate, type) = parseTypeAndDate(doc.select("div.vng-details div.vng-detail-content-beginn").text())
        val pictureUrl = "https://spinnerei.kulturpark.at" + parsePictureUrl(
            doc.select("div.vng-details div.bg-image").attr("style")
        )

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, doc.select("div.vng-detail-content-bodytext").text())
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Spinnerei")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://spinnerei.kulturpark.at/"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Traun")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
        }
    }

    private fun parsePictureUrl(style: String): String {
        //looks like background-image:url('/media/47704/c-wolf-gruber_hypnotica_pr-foto.png?center=0.39316239316239315,0.22666666666666666&mode=crop&width=320&height=180');
        return style.substring(22, style.length - 3)
    }

    private fun parseTypeAndDate(text: String): Pair<OffsetDateTime, String> {
        val split = text.split(' ', ignoreCase = false, limit = 4)
        if (split.size != 4) {
            throw IllegalStateException("could not parse type and date from $text")
        }

        val date = LocalDate.parse(split[1], DateTimeFormatter.ofPattern("dd.MM.uu"))
        val time = LocalTime.parse(split[2], DateTimeFormatter.ofPattern("kk:mm"))
        return Pair(date.atTime(time).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(), split[3])
    }

}
