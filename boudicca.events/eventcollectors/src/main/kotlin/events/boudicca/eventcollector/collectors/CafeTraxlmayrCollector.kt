package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.StructuredEventBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

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

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent?>? {
        val title = event.select("h3").text()
        return when {
            title.contains("|") -> {
                parseConcert(title, event)
            }

            title == "Die nächsten Lesungen im Café Traxlmayr" -> {
                parseLesungen(event)
            }

            title.contains("aktuelle Ausstellung im Café Traxlmayr") -> {
                // ignore
                emptyList()
            }

            else -> {
                logger.error { "unknown event format: $event" }
                emptyList()
            }
        }
    }

    private fun StructuredEventBuilder.applyCommonProperties(eventData: Element) {
        withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(baseUrl))
        withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
        withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Café Traxlmayr")
        withProperty(
            SemanticKeys.LOCATION_URL_PROPERTY,
            UrlUtils.parse("https://www.cafe-traxlmayr.at/"),
        )
        withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
        withProperty(
            SemanticKeys.PICTURE_URL_PROPERTY,
            UrlUtils.parse(eventData.select("img").attr("src")),
        )
        withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Cafe Traxlmayr")
    }

    private fun parseConcert(
        title: String,
        eventData: Element,
    ): List<StructuredEvent> {
        val split = title.split(" | ")
        val name = split[0].trim()
        val bodyLines = eventData.select(".modal-body p strong").textNodes().toList()

        val fullDateText = bodyLines.first { it.text().contains("Uhr") }

        val startDate = DateParser.parse(fullDateText.text().trim())

        val event =
            structuredEvent(name, startDate) {
                applyCommonProperties(eventData)
                withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventData.select(".modal-body").text())
            }
        return event
    }

    private fun parseLesungen(eventData: Element): List<StructuredEvent> {
        val contentBlocks = eventData.select(".modal-body > *")
        var startDate: DateParserResult? = null
        var name = ""
        var description = ""
        var pictureSrc = ""
        val events = ArrayList<StructuredEvent>()

        for (block in contentBlocks) {
            val text = block.text()
            when {
                block.select("img").isNotEmpty() -> pictureSrc = block.select("img").attr("src")
                text.contains("Uhr") -> startDate = DateParser.parse(text)
                text.contains("„") -> name = text
                else -> description += text
            }
            if (name.isEmpty()) {
                name = block.select("strong").joinToString(" ") { it.text() }
            }
            if (startDate != null && name.isNotEmpty() && description.isNotEmpty()) {
                events.addAll(
                    structuredEvent(name, startDate) {
                        applyCommonProperties(eventData)
                        withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
                        withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(baseUrl, pictureSrc))
                    },
                )
                startDate = null
                name = ""
                description = ""
                pictureSrc = ""
            }
        }
        return events
    }
}
