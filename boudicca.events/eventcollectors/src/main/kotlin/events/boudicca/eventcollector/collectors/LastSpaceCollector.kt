package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import events.boudicca.eventcollector.util.fetchUrlAndParse
import events.boudicca.eventcollector.util.withDescription
import java.time.YearMonth

class LastSpaceCollector : TwoStepEventCollector<String>("lastspace") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://last-space.at/"

    override fun getAllUnparsedEvents(): List<String> =
        getNext6Months()
            .map {
                fetcher.fetchUrlAndParse("${baseUrl}index.php?page=event&filter_year_month=$it")
            }.flatMap {
                it.select("div.work-calendar-entry a")
            }.map {
                it.attr("abs:href")
            }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val document = fetcher.fetchUrlAndParse(event)
        val name = document.select("h1").text()
        val description = document.select("div.event-description")

        val startDate = DateParser.parse(document.select("div.event-date").text(), document.select("div.event-time").text())

        val imgSrc = document.select(".slide-img img").attr("abs:src")
        val tags = document.select("div.event-detail-category-tag").map { it.text().removePrefix("#") }
        val type = tags[0]
        val category =
            if (type.lowercase().contains("sport")) {
                EventCategory.SPORT
            } else {
                EventCategory.OTHER
            }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, event)
            withProperty(SemanticKeys.SOURCES_PROPERTY, baseUrl)
            withDescription(description)
            withProperty(SemanticKeys.CATEGORY_PROPERTY, category)
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.TAGS_PROPERTY, tags)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, imgSrc)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "last")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "last")
        }
    }

    private fun getNext6Months(): List<YearMonth> =
        (0L until 6L)
            .map { YearMonth.now().plusMonths(it) }
}
