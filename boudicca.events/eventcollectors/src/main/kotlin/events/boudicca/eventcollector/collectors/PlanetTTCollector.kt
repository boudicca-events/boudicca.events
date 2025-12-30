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
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.StringReader
import java.net.URI
import java.net.URLDecoder
import java.util.regex.Pattern

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["planettt"])
class PlanetTTCollector : TwoStepEventCollector<Element>("planettt") {
    private val logger = KotlinLogging.logger {}
    private val fetcher = FetcherFactory.newFetcher()
    private var modalNonce: String? = null

    override fun getAllUnparsedEvents(): List<Element> {
        val nonces = fetchNonces()
        modalNonce = nonces.second
        val response =
            fetcher.fetchUrlPost(
                "https://planet.tt/wp-admin/admin-ajax.php",
                "application/x-www-form-urlencoded; charset=UTF-8",
                "action=pl_events_list&_ajax_nonce=${nonces.first}&start=0&length=200&search=&location=&eventid=-1",
            )
        val jsonResponse = Parser.default().parse(StringReader(response)) as JsonObject
        val events = Jsoup.parse(jsonResponse.obj("data")!!.string("events")!!)
        return events.select("div.pl-card")
    }

    private fun fetchNonces(): Pair<String, String> {
        val mainSite = fetcher.fetchUrl("https://planet.tt/")
        val javascript = Jsoup.parse(mainSite).select("script#em-events-script-js-extra").html()
        val listPattern = Pattern.compile(".*\"list_nonce\":\"(\\w+)\".*")
        val listMatcher = listPattern.matcher(javascript)
        val modalPattern = Pattern.compile(".*\"modal_nonce\":\"(\\w+)\".*")
        val modalMatcher = modalPattern.matcher(javascript)
        if (listMatcher.find() && modalMatcher.find()) {
            return Pair(listMatcher.group(1), modalMatcher.group(1))
        } else {
            error("could not parse nonces from script")
        }
    }

    override fun parseMultipleStructuredEvents(event: Element): List<StructuredEvent?> {
        val eventId = event.attr("data-eventid")
        val postId = event.attr("data-postid")
        val response =
            fetcher.fetchUrlPost(
                "https://planet.tt/wp-admin/admin-ajax.php",
                "application/x-www-form-urlencoded; charset=UTF-8",
                "action=pl_events_modal&_ajax_nonce=$modalNonce&eventid=$eventId&postid=$postId",
            )
        val jsonResponse = Parser.default().parse(StringReader(response)) as JsonObject
        val fullEvent = Jsoup.parse(jsonResponse.string("data")!!)

        val startDate = parseDate(fullEvent)

        val name = fullEvent.select("div.pl-modal-name").text()
        val url = parseUrl(fullEvent)

        val imgTag = fullEvent.selectFirst("div.pl-modal-thumbnail img")
        val pictureUrl = imgTag?.attr("src")
        val pictureAltText = imgTag?.attr("alt")

        // TODO you could parse acts from this site

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, pictureAltText)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Planet Music")
            withProperty(
                SemanticKeys.DESCRIPTION_TEXT_PROPERTY,
                fullEvent
                    .select("div.pl-modal-desc > p")
                    .text() + "\n" + fullEvent.select("div.pl-modal-desc > div.acts").text(),
            )
            withProperty(SemanticKeys.TYPE_PROPERTY, "concert")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withLocation(fullEvent)
        }
    }

    private fun parseUrl(fullEvent: Document): String {
        val facebookShareUrl = fullEvent.select("input.pl-share-info").attr("data-href")
        val uriQuery = URI(facebookShareUrl).query
        for (queryPart in uriQuery.split("&")) {
            val keyValue = queryPart.split("=", limit = 2)
            if (keyValue.size == 2 && keyValue[0] == "u") {
                return URLDecoder.decode(keyValue[1], Charsets.UTF_8)
            }
        }
        return ""
    }

    private fun StructuredEventBuilder.withLocation(event: Element) {
        val location = event.select("div.pl-modal-location").attr("data-location")
        when (location) {
            "simmcity" -> {
                this
                    .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "SiMMCity")
                    .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://simmcity.at/"))
                    .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
            }

            "szene" -> {
                this
                    .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Szene")
                    .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://szene.wien/"))
                    .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
            }

            "planet" -> {
                this
                    .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Gasometer")
                    .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.gasometer.at/"))
                    .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
            }

            else -> {
                logger.warn { "could not guess location from location: $location" }
            }
        }
    }

    private fun parseDate(event: Element): DateParserResult {
        val date = event.select("span.date").text()
        val time = event.select("span.start").text()

        return DateParser.parse(date, time)
    }

    override fun cleanup() {
        modalNonce = null
    }
}
