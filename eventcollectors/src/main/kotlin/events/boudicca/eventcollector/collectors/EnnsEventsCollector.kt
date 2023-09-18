package events.boudicca.eventcollector.collectors

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.Fetcher
import events.boudicca.api.eventcollector.TwoStepEventCollector
import java.io.StringReader
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class EnnsEventsCollector : TwoStepEventCollector<JsonObject>("ennsevents") {

    override fun getAllUnparsedEvents(): List<JsonObject> {
        val fetcher = Fetcher()
        val unparsedJson =
            fetcher.fetchUrl("https://erlebe.enns.at/snpapi/search/v2?rows=400&start=0&q=&scope=ennsportal&sort=datum&type=EVENT")
        val jsonObject = Parser.default().parse(StringReader(unparsedJson)) as JsonObject
        return jsonObject.array<JsonObject>("items")!!.toList()
    }

    override fun parseEvent(event: JsonObject): Event {

        val name = event.string("title")!!
        val startDate = parseDate(event)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = "https://erlebe.enns.at/events/e/" + event.string("id")
        data[SemanticKeys.DESCRIPTION] = (event.string("subtitle") + "\n" + event.string("description")).trim()

        if (event.containsKey("picture")) {
            data[SemanticKeys.PICTUREURL] =
                "https://erlebe.enns.at/uploads/images/thumbs_square/" + event.string("picture")
        }

        if (event.containsKey("website") && !event.string("website").isNullOrBlank()) {
            data[SemanticKeys.LOCATION_URL] = event.string("website")!!
        }

        return Event(name, startDate, data)
    }

    private fun parseDate(element: JsonObject): OffsetDateTime {
        return OffsetDateTime.parse(element.string("startDate"), DateTimeFormatter.ISO_DATE_TIME)
    }

}
