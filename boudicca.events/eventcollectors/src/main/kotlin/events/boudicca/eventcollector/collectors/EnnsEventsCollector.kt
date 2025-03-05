package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.StringReader
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class EnnsEventsCollector : TwoStepEventCollector<JsonObject>("ennsevents") {

    override fun getAllUnparsedEvents(): List<JsonObject> {
        val fetcher = FetcherFactory.newFetcher()
        val unparsedJson =
            fetcher.fetchUrl("https://erlebe.enns.at/snpapi/search/v2?rows=400&start=0&q=&scope=ennsportal&sort=datum&type=EVENT")
        val jsonObject = Parser.default().parse(StringReader(unparsedJson)) as JsonObject
        return jsonObject.array<JsonObject>("items")!!.toList()
    }

    override fun parseStructuredEvent(event: JsonObject): StructuredEvent {
        val name = event.string("title")!!
        val startDate = parseDate(event)
        val url = "https://erlebe.enns.at/events/e/" + event.string("id")
        val description = (event.string("subtitle") + "\n" + event.string("description")).trim()

        val locationUrl = if (event.containsKey("website") && !event.string("website").isNullOrBlank()) {
            UrlUtils.parse(event.string("website")!!)
        } else {
            null
        }

        val pictureUrl = if (event.containsKey("picture")) UrlUtils.parse(
            "https://erlebe.enns.at/uploads/images/thumbs_square/" + event.string("picture")
        ) else null

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, locationUrl)
        }
    }

    private fun parseDate(element: JsonObject): OffsetDateTime {
        return OffsetDateTime.parse(element.string("startDate"), DateTimeFormatter.ISO_DATE_TIME)
    }

}
