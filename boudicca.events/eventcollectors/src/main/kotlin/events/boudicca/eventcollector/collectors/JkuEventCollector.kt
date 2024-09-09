package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup

class JkuEventCollector : IcalCollector("jku") {
    private val baseUrl = "https://www.jku.at/studium/studieninteressierte/messen-events/"

    override fun getAllIcalResources(): List<String> {
        val fetcher = Fetcher()
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))
        val eventUrls = document.select("div.news_list_item a").eachAttr("href")

        return eventUrls
            .flatMap {
                val eventJson = Jsoup.parse(fetcher.fetchUrl("https://www.jku.at$it"))
                eventJson.select("a").eachAttr("href")
            }
            .filter { it.endsWith(".ics") }
            .map { fetcher.fetchUrl("https://www.jku.at$it") }
    }

    override fun postProcess(event: StructuredEvent): StructuredEvent {
        return event
            .toBuilder()
            .withProperty(SemanticKeys.TAGS_PROPERTY, listOf("JKU", "Universität", "Studieren"))
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            .build()
    }

}
