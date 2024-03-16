package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.lookup
import org.jsoup.Jsoup
import java.io.StringReader
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class KupfTicketCollector : TwoStepEventCollector<String>("kupfticket") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val eventSlugs = mutableSetOf<String>()
        var currentUrl: String? = "https://kupfticket.com/events"

        while (currentUrl != null) {
            val document = Jsoup.parse(fetcher.fetchUrl(currentUrl))
            val nextData = document.select("body script#__NEXT_DATA__").first()!!.data()
            val jsonObject = Parser.default().parse(StringReader(nextData)) as JsonObject

            eventSlugs.addAll(jsonObject.lookup("props.pageProps.events.edges.node.slug"))

            val hasNext = jsonObject.lookup<Boolean>("props.pageProps.events.pageInfo.hasNextPage").first()
            currentUrl = if (hasNext) {
                "https://kupfticket.com/events/cursor/after/" +
                        jsonObject.lookup<String>("props.pageProps.events.pageInfo.endCursor").first()
            } else {
                null
            }
        }

        return eventSlugs.toList()
    }

    override fun parseEvent(event: String): Event {
        val eventSite = Jsoup.parse(fetcher.fetchUrl("https://kupfticket.com/events/$event"))
        val nextData = eventSite.select("body script#__NEXT_DATA__").first()!!.data()
        val jsonObject = Parser.default().parse(StringReader(nextData)) as JsonObject
        val eventJson = jsonObject.lookup<JsonObject>("props.pageProps.event").first()

        val name = eventJson["title"] as String
        val description = eventJson["description"] as String
        val url = "https://kupfticket.com/events/" + (eventJson["slug"] as String)
        val location = eventJson.lookup<String>("location.title").first()
        val locationUrl = eventJson.lookup<String>("shop.websiteLink").first() //TODO check if this is always right?
        val pictureUrl = eventJson.lookup<String>("image.src").first()
        val startDate = parseDate(eventJson.lookup<String>("date.start").first())
        val endDate = parseDate(eventJson.lookup<String>("date.end").first())

        return Event(
            name, startDate,
            mapOf(
                SemanticKeys.DESCRIPTION to description,
                SemanticKeys.URL to url,
                SemanticKeys.LOCATION_NAME to location,
                SemanticKeys.LOCATION_URL to locationUrl,
                SemanticKeys.PICTUREURL to pictureUrl,
                SemanticKeys.ENDDATE to endDate.format(DateTimeFormatter.ISO_DATE_TIME),
                SemanticKeys.SOURCES to url,
            )
        )
    }

    private fun parseDate(date: String): OffsetDateTime {
        return OffsetDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
    }

}
