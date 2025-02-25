package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class CafeTraxlmayrCollector : TwoStepEventCollector<Element>("cafetraxlmayr") {
    private val logger = KotlinLogging.logger {}
    private val baseUrl = "https://www.cafe-traxlmayr.at/konzerte/"

    override fun getAllUnparsedEvents(): List<Element> {
        val fetcher = FetcherFactory.newFetcher()

        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))
        return document
            .select("div.fusion-text a.fusion-modal-text-link")
            .map { it.attr("data-target") }
            .map { document.select(it).first()!! }
    }

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent> {
        val builder = StructuredEvent
            .builder()
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(baseUrl))
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Café Traxlmayr")
            .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.cafe-traxlmayr.at/"))
            .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(event.select("img").attr("src")))
        val title = event.select("h3").text()
        return if (title.contains("|")) {
            parseConcert(title, event, builder)
        } else if (title == "Die nächsten Lesungen im Café Traxlmayr") {
            parseLesungen(event, builder)
        } else if (title.contains("aktuelle Ausstellung im Café Traxlmayr")) {
            //ignore
            emptyList()
        } else {
            logger.error { "unknown event format: $event" }
            emptyList()
        }
    }

    private fun parseConcert(
        title: String,
        event: Element,
        builder: StructuredEvent.StructuredEventBuilder
    ): List<StructuredEvent> {
        val split = title.split(" | ")
        val name = split[0].trim()
        val bodyLines = event.select(".modal-body p strong").textNodes()

        val fullDateText = bodyLines.first { it.text().contains("Uhr") }

        val startDate = parseDateForConcert(fullDateText.text().trim())

        builder
            .withName(name)
            .withStartDate(startDate)
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, event.select(".modal-body").text())

        return listOf(builder.build())
    }

    private fun parseLesungen(event: Element, builder: StructuredEvent.StructuredEventBuilder): List<StructuredEvent> {
        val contentBlocks = event.select(".modal-body > *")
        var startDate = OffsetDateTime.MIN
        var name = ""
        var description = ""
        var pictureSrc = ""
        val events = ArrayList<StructuredEvent>();

        for (block in contentBlocks) {
            val text = block.text()
            if (block.select("img").isNotEmpty()) {
                pictureSrc = block.select("img").attr("src")
            } else if (text.contains("Uhr")) {
                startDate = parseDateForLesung(text)
            } else if (text.contains("„")) {
                name = text
            } else {
                description += text
            }
            if (startDate != OffsetDateTime.MIN && name.isNotEmpty() && description.isNotEmpty() && pictureSrc.isNotEmpty()) {
                val pictureUrl = if (pictureSrc.isNotBlank()) {
                    UrlUtils.parse(baseUrl + pictureSrc)
                } else {
                    null
                }
                events.add(
                    builder
                        .copy()
                        .withName(name)
                        .withStartDate(startDate)
                        .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
                        .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
                        .build()
                )
                startDate = OffsetDateTime.MIN
                name = ""
                description = ""
                pictureSrc = ""
            }
        }
        return events
    }

    private fun parseDateForLesung(fullDateText: String): OffsetDateTime {
        val split = fullDateText.split(',')
        val dateText = split[0].trim()
        val timeText = split[1].trim().substringBefore("Uhr").trim()

        val date = LocalDate.parse(
            dateText,
            DateTimeFormatter.ofPattern("d. LLLL uuuu").withLocale(Locale.GERMAN)
        )
        val time = LocalTime.parse(
            timeText.replace(".", ":"),
            DateTimeFormatter.ofPattern("kk:mm").withLocale(Locale.GERMAN)
        )

        return date.atTime(time).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

    private fun parseDateForConcert(fullDateText: String): OffsetDateTime {
        val split = fullDateText.split(',', '|')
        val dateText = split[1].trim()
        val timeText = split[2].trim()

        val date = LocalDate.parse(
            dateText.replace("Jänner", "Januar"),
            DateTimeFormatter.ofPattern("d. LLLL uuuu").withLocale(Locale.GERMAN)
        )
        val time = LocalTime.parse(
            timeText,
            DateTimeFormatter.ofPattern("kk.mm 'Uhr'").withLocale(Locale.GERMAN)
        )

        return date.atTime(time).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
