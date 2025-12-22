package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

class WissensturmCollector : TwoStepEventCollector<Pair<String, Document>>("wissensturm") {
    override fun getAllUnparsedEvents(): List<Pair<String, Document>> {
        val fetcher = FetcherFactory.newFetcher()

        val eventUrls = mutableListOf<String>()
        var date = LocalDate.now(ZoneId.of("Europe/Vienna"))

        // only collect 6 months for now
        for (ignored in 1..6) {
            val monthlyOverview =
                Jsoup.parse(
                    fetcher.fetchUrl(
                        "https://vhskurs.linz.at/index.php?kathaupt=109&blkeep=1" +
                            "&month=${date.monthValue}&year=${date.year}",
                    ),
                )
            eventUrls.addAll(monthlyOverview.select("div.kurse_demn article a").eachAttr("href"))
            date = date.plusMonths(1)
        }

        return eventUrls
            .map { "https://vhskurs.linz.at/$it" }
            .map { Pair(it, Jsoup.parse(fetcher.fetchUrl(it))) }
    }

    override fun parseMultipleStructuredEvents(event: Pair<String, Document>): List<StructuredEvent> {
        val (url, eventDoc) = event

        val name = eventDoc.select("div.kw-kurdetails h1").text()
        val datesAndLocations = parseDatesAndLocations(eventDoc)

        val description = eventDoc.select("div.kw-kurdetails div.content-txt:nth-child(2)").text()

        val img = eventDoc.select("div.kw-kurdetails div.content-txt:nth-child(2) img")
        val pictureUrl = UrlUtils.parse(img.attr("src"))
        val pictureAltText = img.attr("alt")
        val pictureCopyright =
            if (pictureAltText.contains("Grafik:")) {
                pictureAltText.split("Grafik:").last().trim()
            } else {
                "Wissensturm"
            }

        return datesAndLocations
            .filterNotNull()
            .map {
                structuredEvent(name, it.first) {
                    withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
                    withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, pictureAltText)
                    withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, pictureCopyright)
                    withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
                    withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
                    withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
                    withProperty(SemanticKeys.ENDDATE_PROPERTY, it.second)

                    if (
                        it.third.contains("wissensturm", ignoreCase = true) ||
                        it.third.contains("WT;", ignoreCase = false)
                    ) {
                        withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Wissensturm")
                        withProperty(
                            SemanticKeys.LOCATION_URL_PROPERTY,
                            UrlUtils.parse("https://wissensturm.linz.at/"),
                        )
                        withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
                    } else {
                        withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, it.third)
                    }
                }
            }
    }

    private fun parseDatesAndLocations(event: Document): List<Triple<OffsetDateTime, OffsetDateTime, String>?> =
        event
            .select("table tbody tr")
            .toList()
            .map {
                val fullDateText = it.child(0).text()
                if (fullDateText.contains("ausfall", true) || fullDateText.contains("zusatz", true)) {
                    null
                } else {
                    val result = DateParser.parse(fullDateText)
                    Triple(
                        result.single().startDate,
                        result.single().endDate!!,
                        it.child(it.childrenSize() - 1).text(),
                    )
                }
            }
}
