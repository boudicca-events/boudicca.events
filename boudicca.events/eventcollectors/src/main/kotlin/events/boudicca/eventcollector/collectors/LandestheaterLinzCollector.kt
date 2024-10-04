package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class LandestheaterLinzCollector :
    TwoStepEventCollector<Triple<Element, Pair<String, Document>, LocalDate>>("landestheater linz") {

    override fun getAllUnparsedEvents(): List<Triple<Element, Pair<String, Document>, LocalDate>> {
        val fetcher = Fetcher()
        val events = mutableListOf<Triple<Element, String, LocalDate>>()

        val document = fetchList(fetcher)
        document.select("section")
            .forEach { section ->
                //sections
                val date = parseDateFromSection(section)
                section.select("div.lth-section-content > div.lth-evitem")
                    .forEach { evitem ->
                        val link = evitem.select("div.lth-evitem-title > a")
                        val linkSeason = link.attr("data-lth-season")
                        val linkEventSetId = link.attr("data-lth-eventsetid")
                        val linkRef = link.attr("data-lth-ref")
                        val url =
                            "https://www.landestheater-linz.at/stuecke/detail?EventSetID=${linkEventSetId}&ref=${linkRef}&spielzeit=${linkSeason}"
                        events.add(Triple(evitem, url, date))
                    }
            }

        val eventUrls = events
            .map { event ->
                val link = event.first.select("div.lth-evitem-title > a")
                val linkSeason = link.attr("data-lth-season")
                val linkEventSetId = link.attr("data-lth-eventsetid")
                val linkRef = link.attr("data-lth-ref")
                "https://www.landestheater-linz.at/stuecke/detail?EventSetID=${linkEventSetId}&ref=${linkRef}&spielzeit=${linkSeason}"
            }
            .toSet()

        val resolvedEventUrls = eventUrls
            .associateWith { Jsoup.parse(fetcher.fetchUrl(it)) }

        return events.map { Triple(it.first, Pair(it.second, resolvedEventUrls[it.second]!!), it.third) }
    }

    private fun parseDateFromSection(section: Element): LocalDate {
        val dateText = section.select("div.lth-section-title > div.lth-evitem-date > span.lth-book").text()
        val cleanedUpText = dateText.replace("Jänner", "Januar") //java does not parse Jänner...
        return LocalDate.parse(cleanedUpText, DateTimeFormatter.ofPattern("dd. MMMM uuuu", Locale.GERMAN))
    }

    private fun fetchList(fetcher: Fetcher): Document {
        val nowDate = LocalDate.now(ZoneId.of("Europe/Vienna"))
        val toDate = nowDate.plusMonths(6)
        val now = nowDate.format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))
        val to = toDate.format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))
        return Jsoup.parse(
            fetcher.fetchUrlPost(
                "https://www.landestheater-linz.at/DE/repos/evoscripts/lth/getEvents",
                "application/x-www-form-urlencoded",
                "cal=${now}&monthTo=${to}".encodeToByteArray()
            )
        )
    }

    override fun parseStructuredEvent(event: Triple<Element, Pair<String, Document>, LocalDate>): StructuredEvent {
        val (overview, site, date) = event

        val name = overview.select("div.lth-evitem-title > a").text()

        val (startDate, endDate) = parseDates(overview, date)

        val builder = StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(site.first))
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(site.first))
            .withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            .withProperty(
                SemanticKeys.DESCRIPTION_TEXT_PROPERTY,
                site.second.select("div.lth-layout-ctr section > h2").first { it.text() == "Stückinfo" }.parent()!!
                    .select("div.lth-section-content").text()
            )
            .withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                UrlUtils.parse(
                    "https://www.landestheater-linz.at" + site.second.select("div.lth-slide img").first()!!.attr("src")
                )
            )
            .withProperty(
                SemanticKeys.TYPE_PROPERTY,
                overview.select("div.lth-evitem-what > div.lth-evitem-type").text()
            )

        val locationName =
            site.second.select("div.lth-layout-ctr > div > div > span > span").get(1).text().substring(11).trim()
        insertLocationData(builder, locationName)

        return builder
            .build()
    }

    private fun insertLocationData(builder: StructuredEvent.StructuredEventBuilder, locationName: String) {
        if (locationName.contains("musiktheater", ignoreCase = true)) {
            builder
                .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Musiktheater Linz")
        } else if (locationName == "Studiobühne") {
            //there is no dedicated page for it, but it is in the same building, so....
            builder
                .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Studiobühne Linz")
        } else if (locationName == "Schauspielhaus") {
            builder
                .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Schauspielhaus Linz")
        } else if (locationName == "Kammerspiele") {
            builder
                .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Kammerspiele Linz")
        } else if (locationName.isNotBlank()) {
            builder.withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, locationName)
        }
    }

    private fun parseDates(overview: Element, date: LocalDate): Pair<OffsetDateTime, OffsetDateTime?> {
        val timeText = overview.select("div.lth-evitem-time").text()
        if (timeText.length > 13) {
            val startTimeText = timeText.substring(0, 5)
            val endTimeText = timeText.substring(8, 13)
            val startTime = LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("kk:mm"))
            val endTime = LocalTime.parse(endTimeText, DateTimeFormatter.ofPattern("kk:mm"))
            return Pair(
                date.atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
                date.atTime(endTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
            )
        } else {
            val startTimeText = timeText.substring(0, 5)
            val startTime = LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("kk:mm"))
            return Pair(
                date.atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
                null
            )
        }
    }

}
