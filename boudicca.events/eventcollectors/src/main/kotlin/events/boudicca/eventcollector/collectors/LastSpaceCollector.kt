package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup

class LastSpaceCollector : TwoStepEventCollector<String>("lastspace") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://last-space.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val pages = Jsoup.parse(fetcher.fetchUrl(baseUrl + "page/event"))
            .select(".pagination li a")
            .mapNotNull { it.attr("href") }
            .filter { it.contains("page") } // to ignore next and previous page buttons
            .distinct()

        val eventUrls = pages
            .flatMap { fetchEventUrlsOfSinglePage(it) }
            .distinct()

        return eventUrls
    }

    private fun fetchEventUrlsOfSinglePage(page: String): List<String> {
        return Jsoup.parse(fetcher.fetchUrl(baseUrl + page))
            .select("tr>td>div>div")
            .mapNotNull {
                baseUrl + it.attr("onClick")
                    .replace("'", "")
                    .replace("window.location.href=/", "")
            }
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val document = Jsoup.parse(fetcher.fetchUrl(event))
        val name = document.select("h1").text()
        val description = document.select("div.event-description").text()

        val eventInfos = document.select("div.event-infos").textNodes()
        val startDate = DateParser.parse(eventInfos[0].text(), eventInfos[1].text())

        val imgSrc = document.select(".slide-img img").attr("src")
        val tags = eventInfos[4].text()
            .split(",")
            .map { it.replace("#", "").trim() }
            .filter { it.isNotBlank() }

        val type = eventInfos[3].text().trim()
        var category = EventCategory.OTHER
        if (type.lowercase().contains("sport")) {
            category = EventCategory.SPORT
        }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.CATEGORY_PROPERTY, category)
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            withProperty(SemanticKeys.TAGS_PROPERTY, tags)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(baseUrl, imgSrc))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "last")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "last")
        }
    }
}
