package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.collectors.IcalCollector
import base.boudicca.model.Event
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

    override fun postProcess(event: Event): Event {
        return Event(event.name, event.startDate,
            event.data.toMutableMap().apply {
                put(SemanticKeys.TAGS, listOf("JKU", "Universität", "Studieren").toString())
                put(SemanticKeys.SOURCES, baseUrl)
            })
    }

}
