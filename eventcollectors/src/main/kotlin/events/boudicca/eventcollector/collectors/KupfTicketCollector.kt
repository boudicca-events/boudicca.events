package events.boudicca.eventcollector.collectors

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.lookup
import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.Fetcher
import events.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.io.StringReader
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class KupfTicketCollector : TwoStepEventCollector<JsonObject>("kupfticket") {

    override fun getAllUnparsedEvents(): List<JsonObject> {
        val fetcher = Fetcher()
        val document = Jsoup.parse(fetcher.fetchUrl("https://kupfticket.com/events"))
        val next_data = document.select("body script#__NEXT_DATA__").first()!!.data()
        val jsonObject = Parser.default().parse(StringReader(next_data)) as JsonObject

        val eventSlugs = jsonObject.lookup<String>("props.pageProps.events.edges.node.slug")

        return eventSlugs
            .asSequence()
            .mapNotNull {
                try {
                    fetcher.fetchUrl("https://kupfticket.com/events/$it")//some links just 404...
                } catch (e: HttpStatusException) {
                    Thread.sleep(1000)
                    try {
                        fetcher.fetchUrl("https://kupfticket.com/events/$it")//what do you mean stable?
                    } catch (e2: HttpStatusException) {
                        println("https://kupfticket.com/events/$it error after retry: " + e2.statusCode)
                        null
                    }
                }
            }
            .map {
                Jsoup.parse(it)
            }
            .map { it.select("body script#__NEXT_DATA__").first()!!.data() }
            .map {
                val evenJsonObject = Parser.default().parse(StringReader(it)) as JsonObject
                evenJsonObject.lookup<JsonObject>("props.pageProps.event").first()
            }
            .toList()
    }

    override fun parseEvent(event: JsonObject): Event {
        val name = event["title"] as String
//        val type = ????? TODO the heck, they have no information on type :(
        val description = event["description"] as String
        val url = "https://kupfticket.com/events/" + (event["slug"] as String)
        val location = event.lookup<String>("location.title").first() //TODO has street and such in name..
        val locationUrl = event.lookup<String>("shop.websiteLink").first() //TODO check if this is always right?
        val pictureUrl = event.lookup<String>("image.src").first()
        val startDate = parseDate(event.lookup<String>("date.start").first())
        val endDate = parseDate(event.lookup<String>("date.end").first())

        return Event(
            name, startDate,
            mapOf(
                SemanticKeys.DESCRIPTION to description,
                SemanticKeys.URL to url,
                SemanticKeys.LOCATION_NAME to location,
                SemanticKeys.LOCATION_URL to locationUrl,
                SemanticKeys.PICTUREURL to pictureUrl,
                SemanticKeys.ENDDATE to endDate.format(DateTimeFormatter.ISO_DATE_TIME),
            )
        )
    }

    private fun parseDate(date: String): OffsetDateTime {
        return OffsetDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
    }

}
