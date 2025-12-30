package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["posthof"])
class PosthofCollector : TwoStepEventCollector<String>("posthof") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.posthof.at"

    override fun getAllUnparsedEvents(): List<String> {
        val eventUrls = mutableListOf<String>()

        var nextUrl: String = baseUrl
        while (true) {
            val currentDoc = Jsoup.parse(fetcher.fetchUrl(nextUrl))
            eventUrls.addAll(currentDoc.select("ul.programmlist li h2 a").map { baseUrl + it.attr("href") })

            val nextButton = currentDoc.select("ul.programmlist li.loadnext button")
            if (nextButton.isNotEmpty()) {
                nextUrl = baseUrl + nextButton.attr("hx-post")
            } else {
                break
            }
        }

        return eventUrls
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val eventSite: Element = Jsoup.parse(fetcher.fetchUrl(event))

        var name =
            eventSite
                .select("div.tx-posthof-events>:not(ul) h2 a")
                .textNodes()
                .first()
                .text()

        val subtextSpan = eventSite.select("div.tx-posthof-events>:not(ul) h2 a span")
        if (subtextSpan.isNotEmpty()) {
            name += " - " + subtextSpan.text()
        }

        val dateAndTypeSpans = getDateAndTypeSpans(eventSite)
        val startDate = DateParser.parse(dateAndTypeSpans[0].text(), dateAndTypeSpans[1].text())

        var description = ""
        val textBlocks = eventSite.select("div.tx-posthof-events>:not(ul)")
        for (child in textBlocks) {
            if (child.tagName() == "hr") {
                break
            }
            description += child.text() + "\n"
        }

        val imgTag = eventSite.select("div.tx-posthof-events>:not(ul) .cell img")
        val imgUrl = baseUrl + imgTag.attr("src")
        val pictureCopyright =
            imgTag
                .attr("title")
                .split("(c)")
                .last()
                .trim()
                .ifBlank { "Posthof" }

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.TYPE_PROPERTY, dateAndTypeSpans[2].text())
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, URI.create(imgUrl))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, pictureCopyright)
            withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.TICKET) // are there free events in posthof?
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Posthof")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, URI.create("https://www.posthof.at"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Linz")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            withProperty(SemanticKeys.URL_PROPERTY, URI.create(event))
        }
    }

    private fun getDateAndTypeSpans(eventSite: Element): Elements {
        val preHeaders = eventSite.select("div.tx-posthof-events>:not(ul) div.pre-header")
        return preHeaders.last()!!.select("span")
    }
}
