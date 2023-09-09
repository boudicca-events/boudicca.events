package events.boudicca.eventcollector.collectors

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.Fetcher
import events.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.io.StringReader
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

    override fun parseEvent(event: Element): Event {

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
        val data = mutableMapOf<String, String>()

        val name = fullEvent.select("div.pl-modal-name").text()
        data[SemanticKeys.URL] = "https://planet.tt/" //TODO this is broken on their site right now -.-
        val pictureUrl = fullEvent.select("div.pl-modal-thumbnail img").attr("src")
        if (pictureUrl.isNotBlank()) {
            data[SemanticKeys.PICTUREURL] = pictureUrl
        }
        data[SemanticKeys.DESCRIPTION] = fullEvent.select("div.pl-modal-desc > p").text() + "\n" +
                fullEvent.select("div.pl-modal-desc > div.acts").text()

        //TODO you could parse acts from this site
        data[SemanticKeys.TYPE] = "concert"
        mapLocation(data, fullEvent)

        return Event(name, startDate, data)
    }

    private fun mapLocation(data: MutableMap<String, String>, event: Element) {
        val location = event.select("div.pl-modal-location").attr("data-location")
        if (location == "simmcity") {
            data[SemanticKeys.LOCATION_NAME] = "SiMMCity"
            data[SemanticKeys.LOCATION_URL] = "https://simmcity.at/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else if (location == "szene") {
            data[SemanticKeys.LOCATION_NAME] = "Szene"
            data[SemanticKeys.LOCATION_URL] = "https://szene.wien/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else if (location == "planet") {
            data[SemanticKeys.LOCATION_NAME] = "Gasometer"
            data[SemanticKeys.LOCATION_URL] = "https://www.gasometer.at/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else {
            LOG.warn("could not guess location from location: $location")
        }
    }

    private fun parseDate(event: Element): OffsetDateTime {
        val date = event.select("span.date").text().split(',', ignoreCase = false, limit = 2)[1].trim()
        val time = event.select("span.start").text().split(':', ignoreCase = false, limit = 2)[1].trim()

        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd. MMMM uuuu", Locale.ENGLISH))
            .atTime(LocalTime.parse(time, DateTimeFormatter.ofPattern("kk:mm", Locale.ENGLISH)))
            .atZone(ZoneId.of("Europe/Vienna"))
            .toOffsetDateTime()
    }

    override fun cleanup() {
        modalNonce = null
    }
}
