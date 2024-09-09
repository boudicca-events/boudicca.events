package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.net.URI
import java.net.URLDecoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

class PlanetTTCollector : TwoStepEventCollector<Element>("planettt") {

    private val LOG = LoggerFactory.getLogger(this::class.java)
    private val fetcher = Fetcher()
    private var modalNonce: String? = null

    override fun getAllUnparsedEvents(): List<Element> {
        val nonces = fetchNonces()
        modalNonce = nonces.second
        val response = fetcher.fetchUrlPost(
            "https://planet.tt/wp-admin/admin-ajax.php",
            "application/x-www-form-urlencoded; charset=UTF-8",
            "action=pl_events_list&_ajax_nonce=${nonces.first}&start=0&length=200&search=&location=&eventid=-1".toByteArray(
                Charsets.UTF_8
            )
        )
        val jsonResponse = Parser.default().parse(StringReader(response)) as JsonObject
        val events = Jsoup.parse(jsonResponse.obj("data")!!.string("events")!!)
        return events.select("div.pl-card")
    }

    private fun fetchNonces(): Pair<String, String> {
        val mainSite = fetcher.fetchUrl("https://planet.tt/")
        val javascript = Jsoup.parse(mainSite).select("script#em-events-script-js-extra").html()
        val listPattern = Pattern.compile(".*\"list_nonce\":\"([\\w\\d]+)\".*")
        val listMatcher = listPattern.matcher(javascript)
        val modalPattern = Pattern.compile(".*\"modal_nonce\":\"([\\w\\d]+)\".*")
        val modalMatcher = modalPattern.matcher(javascript)
        if (listMatcher.find() && modalMatcher.find()) {
            return Pair(listMatcher.group(1), modalMatcher.group(1))
        } else {
            throw RuntimeException("could not parse nonces from script")
        }
    }

    override fun parseStructuredEvent(event: Element): StructuredEvent {

        val eventId = event.attr("data-eventid")
        val postId = event.attr("data-postid")
        val response = fetcher.fetchUrlPost(
            "https://planet.tt/wp-admin/admin-ajax.php",
            "application/x-www-form-urlencoded; charset=UTF-8",
            "action=pl_events_modal&_ajax_nonce=${modalNonce}&eventid=$eventId&postid=$postId".toByteArray(
                Charsets.UTF_8
            )
        )
        val jsonResponse = Parser.default().parse(StringReader(response)) as JsonObject
        val fullEvent = Jsoup.parse(jsonResponse.string("data")!!)

        val startDate = parseDate(fullEvent)

        val name = fullEvent.select("div.pl-modal-name").text()
        val url = parseUrl(fullEvent)
        val pictureUrl = fullEvent.select("div.pl-modal-thumbnail img").attr("src")

        //TODO you could parse acts from this site

        val builder = StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            .withProperty(
                SemanticKeys.DESCRIPTION_TEXT_PROPERTY,
                fullEvent.select("div.pl-modal-desc > p")
                    .text() + "\n" + fullEvent.select("div.pl-modal-desc > div.acts").text()
            )
            .withProperty(SemanticKeys.TYPE_PROPERTY, "concert")
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))

        mapLocation(builder, fullEvent)

        return builder
            .build()
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

    private fun mapLocation(builder: StructuredEvent.StructuredEventBuilder, event: Element) {
        val location = event.select("div.pl-modal-location").attr("data-location")
        when (location) {
            "simmcity" -> {
                builder
                    .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "SiMMCity")
                    .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://simmcity.at/"))
                    .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
            }

            "szene" -> {
                builder
                    .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Szene")
                    .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://szene.wien/"))
                    .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
            }

            "planet" -> {
                builder
                    .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Gasometer")
                    .withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://www.gasometer.at/"))
                    .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
            }

            else -> {
                LOG.warn("could not guess location from location: $location")
            }
        }
    }

    private fun parseDate(event: Element): OffsetDateTime {
        val date = event.select("span.date").text().split(',', ignoreCase = false, limit = 2)[1].trim()
        val time = event.select("span.start").text().split(':', ignoreCase = false, limit = 2)[1].trim()

        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd. MMMM uuuu", Locale.GERMAN))
            .atTime(LocalTime.parse(time, DateTimeFormatter.ofPattern("kk:mm", Locale.GERMAN)))
            .atZone(ZoneId.of("Europe/Vienna"))
            .toOffsetDateTime()
    }

    override fun cleanup() {
        modalNonce = null
    }
}
