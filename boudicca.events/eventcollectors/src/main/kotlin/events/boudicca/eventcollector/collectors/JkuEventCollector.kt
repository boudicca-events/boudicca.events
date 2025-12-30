package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["jku"])
class JkuEventCollector : IcalCollector("jku") {
    private val baseUrl = "https://www.jku.at/studium/studieninteressierte/messen-events/"

    override fun getAllIcalResources(): List<String> {
        val fetcher = FetcherFactory.newFetcher()
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))
        val eventUrls = document.select("div.news_list_item a").eachAttr("href")

        return eventUrls
            .flatMap {
                try {
                    val eventJson = Jsoup.parse(fetcher.fetchUrl("https://www.jku.at$it"))
                    eventJson.select("a").eachAttr("href")
                } catch (_: RuntimeException) {
                    mutableListOf<String>() // skip faulty links that result in 404
                }
            }.filter { it.endsWith(".ics") }
            .map { fetcher.fetchUrl("https://www.jku.at$it") }
            .distinct()
    }

    override fun postProcess(event: StructuredEvent): StructuredEvent =
        event
            .toBuilder()
            .withProperty(SemanticKeys.TAGS_PROPERTY, listOf("JKU", "Universität", "Studieren"))
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl))
            .build()
}
