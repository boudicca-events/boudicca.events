package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.fetcher.Fetcher
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.StructuredEventBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["landestheaterlinz"])
class LandestheaterLinzCollector : TwoStepEventCollector<LandestheaterLinzCollector.LandestheaterEventData>("landestheater linz") {
    private val baseUrl = "https://www.landestheater-linz.at"

    data class LandestheaterEventData(
        val eventItem: Element,
        val document: Pair<String, Document>,
        val dateText: String,
    )

    override fun getAllUnparsedEvents(): List<LandestheaterEventData> {
        val fetcher = FetcherFactory.newFetcher()
        val events = mutableListOf<Triple<Element, String, String>>()

        val document = fetchList(fetcher)
        document
            .select("section")
            .forEach { section ->
                // sections
                val dateText = section.select("div.lth-section-title > div.lth-evitem-date > span.lth-book").text()
                section
                    .select("div.lth-section-content > div.lth-evitem")
                    .forEach { evitem ->
                        val link = evitem.select("div.lth-evitem-title > a")
                        val linkSeason = link.attr("data-lth-season")
                        val linkEventSetId = link.attr("data-lth-eventsetid")
                        val linkRef = link.attr("data-lth-ref")
                        val url =
                            "$baseUrl/stuecke/detail?EventSetID=$linkEventSetId&ref=$linkRef&spielzeit=$linkSeason"
                        events.add(Triple(evitem, url, dateText))
                    }
            }

        val eventUrls =
            events
                .map { event ->
                    val link = event.first.select("div.lth-evitem-title > a")
                    val linkSeason = link.attr("data-lth-season")
                    val linkEventSetId = link.attr("data-lth-eventsetid")
                    val linkRef = link.attr("data-lth-ref")
                    "$baseUrl/stuecke/detail?EventSetID=$linkEventSetId&ref=$linkRef&spielzeit=$linkSeason"
                }.toSet()

        val resolvedEventUrls =
            eventUrls
                .associateWith { Jsoup.parse(fetcher.fetchUrl(it)) }

        return events.map {
            LandestheaterEventData(
                it.first,
                it.second to resolvedEventUrls.getValue(it.second),
                it.third,
            )
        }
    }

    private fun fetchList(fetcher: Fetcher): Document {
        val nowDate = LocalDate.now(ZoneId.of("Europe/Vienna"))
        val toDate = nowDate.plusMonths(6)
        val now = nowDate.format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))
        val to = toDate.format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))
        return Jsoup.parse(
            fetcher.fetchUrlPost(
                "$baseUrl/DE/repos/evoscripts/lth/getEvents",
                "application/x-www-form-urlencoded",
                "cal=$now&monthTo=$to",
            ),
        )
    }

    override fun parseMultipleStructuredEvents(event: LandestheaterEventData): List<StructuredEvent?> {
        val (overview, site, date) = event

        val name = overview.select("div.lth-evitem-title > a").text()

        val dates = parseDates(overview, date)

        val locationName =
            site.second
                .select("div.lth-layout-ctr > div > div > span > span")[1]
                .text()
                .substring(11)
                .trim()

        val structuredEvent =
            structuredEvent(name, dates) {
                withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(site.first))
                withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(site.first))
                withProperty(
                    SemanticKeys.DESCRIPTION_TEXT_PROPERTY,
                    site.second
                        .select("div.lth-layout-ctr section > h2")
                        .first { it.text() == "Stückinfo" }
                        .parent()!!
                        .select("div.lth-section-content")
                        .text(),
                )
                withProperty(
                    SemanticKeys.PICTURE_URL_PROPERTY,
                    UrlUtils.parse(baseUrl, site.second.select("div.lth-slide img").attr("src")),
                )
                withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Landestheater Linz")
                withProperty(
                    SemanticKeys.TYPE_PROPERTY,
                    overview.select("div.lth-evitem-what > div.lth-evitem-type").text(),
                )
                withLocationData(locationName)
            }

        return structuredEvent
    }

    private fun StructuredEventBuilder.withLocationData(locationName: String) {
        when {
            locationName.contains("musiktheater", ignoreCase = true) -> {
                this.withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Musiktheater Linz")
            }

            locationName == "Studiobühne" -> {
                // there is no dedicated page for it, but it is in the same building, so....
                this.withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Studiobühne Linz")
            }

            locationName == "Schauspielhaus" -> {
                this.withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Schauspielhaus Linz")
            }

            locationName == "Kammerspiele" -> {
                this.withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Kammerspiele Linz")
            }

            locationName.isNotBlank() -> {
                this.withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, locationName)
            }
        }
    }

    private fun parseDates(
        overview: Element,
        dateText: String,
    ): DateParserResult = DateParser.parse(dateText, overview.select("div.lth-evitem-time").text())
}
