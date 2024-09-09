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

class OteloLinzCollector : TwoStepEventCollector<String>("otelolinz") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://www.otelolinz.at/veranstaltungen/"))
        return document.select("table.events-table tr a")
            .map { it.attr("href") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("div.article-inner h1").text()
        val (startDate, endDate) = parseDates(eventSite)

        val img = eventSite.select("div.entry-content img")
        val pictureUrl = if (!img.isEmpty()) {
            UrlUtils.parse(img.first()!!.attr("src"))
        } else {
            null
        }

        return StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            .withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            .withProperty(SemanticKeys.TYPE_PROPERTY, "technology")
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, getDescription(eventSite))
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            .withProperty(
                SemanticKeys.LOCATION_NAME_PROPERTY,
                eventSite.select("div#em-event-6>p")[1].select("a").text()
            )
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            .build()
    }

    private fun getDescription(eventSite: Document): String {
        val sb = StringBuilder()

        var foundBreak = false
        for (e in eventSite.select("div#em-event-6").first()!!.children()) {
            if (e.tagName() == "br") {
                foundBreak = true
            } else {
                if (foundBreak) {
                    val text = e.text()
                    if (text.isNotBlank()) {
                        sb.append(text)
                        sb.appendLine()
                    }
                }
            }
        }

        return sb.toString()
    }

    private fun parseDates(element: Element): Pair<OffsetDateTime, OffsetDateTime?> {
        val fullDate = element.select("div#em-event-6>p").first()!!.textNodes()[1].text().trim()
        val date = fullDate.substring(fullDate.length - 10)
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.LL.uuuu"))

        val startAndEndTimeText = element.select("div#em-event-6>p").first()!!.select("i").text()
        if (startAndEndTimeText == "0:00") {
            return Pair(
                localDate.atStartOfDay().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
                null
            )
        }

        val startAndEndTimes = startAndEndTimeText.split(" - ")

        val localStartTime = LocalTime.parse(startAndEndTimes[0].trim(), DateTimeFormatter.ofPattern("kk:mm"))
        val localEndTime = if (startAndEndTimes.size > 1) {
            LocalTime.parse(startAndEndTimes[1].trim(), DateTimeFormatter.ofPattern("kk:mm"))
        } else {
            null
        }

        return Pair(
            localDate.atTime(localStartTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime(),
            if (localEndTime != null) {
                localDate.atTime(localEndTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
            } else {
                null
            }
        )
    }

}
