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

class WissensturmCollector : TwoStepEventCollector<Pair<String, Document>>("wissensturm") {

    override fun getAllUnparsedEvents(): List<Pair<String, Document>> {
        val fetcher = FetcherFactory.newFetcher()

        val eventUrls = mutableListOf<String>()
        var date = LocalDate.now(ZoneId.of("Europe/Vienna"))

        //only collect 6 months for now
        for (i in 1..6) {
            val monthlyOverview =
                Jsoup.parse(fetcher.fetchUrl("https://vhskurs.linz.at/index.php?kathaupt=109&blkeep=1&month=${date.monthValue}&year=${date.year}"))
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
        val pictureUrl = if (img.isNotEmpty()) {
            UrlUtils.parse(img.attr("src"))
        } else {
            null
        }

        return datesAndLocations
            .filter { it.first != null }
            .map {
                structuredEvent(name, it.first!!) {
                    withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
                    withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
                    withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
                    withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
                    withProperty(SemanticKeys.ENDDATE_PROPERTY, it.second)

                    if (
                        it.third.contains("wissensturm", ignoreCase = true)
                        || it.third.contains("WT;", ignoreCase = false)
                    ) {
                        withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Wissensturm")
                        withProperty(
                            SemanticKeys.LOCATION_URL_PROPERTY,
                            UrlUtils.parse("https://wissensturm.linz.at/")
                        )
                        withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
                    } else {
                        withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, it.third)
                    }
                }
            }
    }

    private fun parseDatesAndLocations(event: Document): List<Triple<OffsetDateTime?, OffsetDateTime?, String>> {
        return event.select("table tbody tr")
            .toList()
            .map {
                val fullDateText = it.child(0).text()
                if (fullDateText.contains("ausfall", true) || fullDateText.contains("zusatz", true)) {
                    Triple(null, null, it.child(it.childrenSize() - 1).text())
                } else {
                    val date = fullDateText.substring(4, 14)
                    val startTime = fullDateText.substring(15, 20)
                    val endTime = fullDateText.substring(23, 28)

                    val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.uuuu"))
                    val localStartTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("kk:mm"))
                    val localEndTime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("kk:mm"))

                    Triple(
                        localDate.atTime(localStartTime)
                            .atZone(ZoneId.of("Europe/Vienna"))
                            .toOffsetDateTime(),
                        localDate.atTime(localEndTime)
                            .atZone(ZoneId.of("Europe/Vienna"))
                            .toOffsetDateTime(),
                        it.child(it.childrenSize() - 1).text()
                    )
                }
            }
    }

}
