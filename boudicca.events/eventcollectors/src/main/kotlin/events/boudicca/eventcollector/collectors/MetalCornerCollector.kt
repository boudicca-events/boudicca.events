package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.format.UrlUtils
import base.boudicca.model.EventCategory
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup

class MetalCornerCollector : TwoStepEventCollector<Pair<String, String>>("metalcorner") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.escape-metalcorner.at/"

    override fun getAllUnparsedEvents(): List<Pair<String, String>> {
        val eventUrls = mutableListOf<Pair<String, String>>()

        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "de/events"))
        document.select("div#content > div#events .event")
            .forEach {
                eventUrls.add(
                    Pair(
                        it.select(".head").text(),
                        it.select("a.overlay").attr("href").substring(2),
                    ),
                )
            }

        return eventUrls
    }

    override fun parseMultipleStructuredEvents(event: Pair<String, String>): List<StructuredEvent?>? {
        val (eventType, url) = event

        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + url))

        val name = document.select("div#content h1").text()

        val eventStartDate = DateParser.parse(document.select("div#content h2").text())

        return structuredEvent(name, eventStartDate) {
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, document.select("div#content p").text())
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(baseUrl, url))
            withProperty(SemanticKeys.TYPE_PROPERTY, eventType)
            withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                UrlUtils.parse(document.select("div#content img").attr("src")),
            )
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, document.select("div#content img").attr("alt"))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Escape Metalcorner")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(baseUrl + url))
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Escape Metalcorner")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
            withProperty(
                SemanticKeys.LOCATION_ADDRESS_PROPERTY,
                "Escape Metalcorner, Neustiftgasse 116-118, 1070 Wien",
            )
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.escape-metalcorner.at/"))
            withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.TICKET)
            withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.MUSIC)
        }
    }
}
