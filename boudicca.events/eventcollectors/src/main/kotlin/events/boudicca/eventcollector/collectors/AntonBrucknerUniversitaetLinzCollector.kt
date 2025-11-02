package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup

class AntonBrucknerUniversitaetLinzCollector : TwoStepEventCollector<String>("antonbrucknerunilinz") {
    private val baseUrl = "https://www.bruckneruni.ac.at"
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val eventUrls = mutableListOf<String>()

        var wantedPage = 2
        var page = Jsoup.parse(fetcher.fetchUrl("$baseUrl/de/events"))
        while (true) {
            eventUrls.addAll(page.select("div.event-list > div > a").map { it.attr("href") })

            val nextPageLink = page.select("nav li.page-item a.page-link")
                .filter { it.text().isNotBlank() }
                .filter { it.text().toInt() == wantedPage }
            if (nextPageLink.isEmpty()) {
                break
            }
            val nextPageUrl = nextPageLink.first().attr("href")
            wantedPage++
            page = Jsoup.parse(fetcher.fetchUrl("$baseUrl$nextPageUrl"))
        }

        return eventUrls
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?> {
        val url = "$baseUrl$event"
        val eventSite = Jsoup.parse(fetcher.fetchUrl(url))

        val name = eventSite.select("h1.event-title").text()

        val content = eventSite.select("div#person-detail-intro > div > div > *")
        var description = ""
        for (child in content) {
            if (child.tagName() == "div") {
                break
            }
            if (child.tagName() == "p") {
                description += "\n" + child.text()
            }
        }
        description = description.trim()

        val tags = eventSite.select("div#person-detail-intro > div > div > p")
            .first { it.text().startsWith("Themen:") }
            .text().removePrefix("Themen:").split(",").map { it.trim() }
        val type = tags.firstOrNull()

        val imgSrc = eventSite.select("div#person-detail-intro figure img").attr("src")

        val dateAndLocation = eventSite.select("div#person-detail-intro > div > div > div > p")
            .first { it.text().startsWith("Wann und Wo:") }
            .text()
        val split = dateAndLocation.indexOf("-", dateAndLocation.indexOf("-") + 1) // get the second "-"

        val startDate = DateParser.parse(dateAndLocation.substring(0, split))
        val location = if (dateAndLocation.length > split + 2) dateAndLocation.substring(split + 2) else null
        val (city, locationUrl) = location?.run { getCityAndUrlOfLocation(location) } ?: Pair(null, null)

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(SemanticKeys.TYPE_PROPERTY, type)
            if (tags.isNotEmpty()) withProperty(SemanticKeys.TAGS_PROPERTY, tags)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(baseUrl, imgSrc))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Anton Bruckner Privatuniversität Linz")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, city)
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(locationUrl))
            withProperty(
                SemanticKeys.LOCATION_NAME_PROPERTY,
                location?.ifBlank { "Anton Bruckner Privatuniversität Linz" }
            )
        }
    }

    private fun getCityAndUrlOfLocation(location: String): Pair<String?, String?> {
        val roomsAtBrucknerUni = listOf("Großer Saal", "Sonic Lab", "Reinhart-von-Gutzeit-Saal", "Studiobühne")
        if (roomsAtBrucknerUni.any { it in location }) {
            return Pair("Linz", baseUrl)
        }

        val cityRegex = """,\s\d{4}\s(?<city>.*)""".toRegex()
        val cityMatchResult = cityRegex.find(location)
        if (cityMatchResult != null) {
            return Pair(cityMatchResult.groupValues.last(), null)
        }

        return Pair(null, null)
    }
}
