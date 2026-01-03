package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.annotations.BoudiccaEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.regex.Pattern

/**
 * the actual eventlist on https://ooesb.at/veranstaltungen is an iframe pointing to https://servicebroker.media-data.at/overview.html?key=QVKSBOOE so we parse that
 */
@BoudiccaEventCollector("ooesb")
class OOESeniorenbundCollector : TwoStepEventCollector<Pair<Document, String>>("ooesb") {
    override fun getAllUnparsedEvents(): List<Pair<Document, String>> {
        val fetcher = FetcherFactory.newFetcher()
        val document = Jsoup.parse(fetcher.fetchUrl("https://servicebroker.media-data.at/overview.html?key=QVKSBOOE"))

        return document
            .select("a.link-detail")
            .toList()
            .map { "https://servicebroker.media-data.at/" + it.attr("href") }
            .map { Pair(Jsoup.parse(fetcher.fetchUrl(it)), it) }
    }

    override fun parseMultipleStructuredEvents(event: Pair<Document, String>): List<StructuredEvent> {
        val (eventDoc, rawUrl) = event

        val url = cleanupUrl(rawUrl)
        val name = eventDoc.select("div.title>p").text()
        val dates = getDates(eventDoc)
        val description = eventDoc.select("div.subtitle>p").text()

        return dates
            .map {
                structuredEvent(name, it) {
                    withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
                    withProperty(
                        SemanticKeys.LOCATION_NAME_PROPERTY,
                        eventDoc.select("div.venue").text(),
                    ) // TODO location name and city here are not seperated at all -.-
                    withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
                    withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
                }
            }.flatten()
    }

    private fun cleanupUrl(url: String): String {
        // https://servicebroker.media-data.at/detail.html;jsessionid=B20D66D14ABACD0C9357ECC77CA10E48?evkey=11774&resize=true&key=QVKSBOOE

        val sessionIdPattern = Pattern.compile(";jsessionid=\\w+\\?")
        val matcher = sessionIdPattern.matcher(url)

        return if (matcher.find()) {
            url.replace(matcher.group(0), "?")
        } else {
            url
        }
    }

    private fun getDates(event: Document): List<DateParserResult> =
        event
            .select("div.date>p")
            .toList()
            .map { getSingleDates(it.text()) }

    private fun getSingleDates(dateString: String): DateParserResult = DateParser.parse(dateString)
}
