package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DatePair
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.StringReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ParlamentCollector : TwoStepEventCollector<JsonObject>("parlament") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.parlament.gv.at/"

    override fun getAllUnparsedEvents(): List<JsonObject> {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateFrom = LocalDate.now().format(dateTimeFormatter)
        val dateTo = LocalDate.now().plusMonths(1).format(dateTimeFormatter)

        val response =
            fetcher.fetchUrlPost(
                baseUrl + "Filter/api/filter/data/600?showAll=true",
                "application/json",
                "{'DATERANGE':['$dateFrom','$dateTo']}",
            )
        val jsonResponse = Parser.default().parse(StringReader(response)) as JsonObject

        return extractEventsFromJson(jsonResponse["rows"] as List<JsonArray<List<String>>>)
    }

    private fun extractEventsFromJson(eventArrays: List<JsonArray<List<String>>>): List<JsonObject> {
        val events = mutableListOf<JsonObject>()
        for (eventArray in eventArrays) {
            val event = JsonObject()
            event.put("name", eventArray[3])
            event.put("url", baseUrl + eventArray[4])
            event.put("locationName", eventArray[8].toString().replace("Parliament", "Parlament"))
            event.put("startDate", eventArray[15])
            event.put("endDate", eventArray[16])
            events.add(event)
        }
        return events
    }

    override fun parseMultipleStructuredEvents(event: JsonObject): List<StructuredEvent?>? {
        var dates = DateParser.parse(event["startDate"].toString())
        if (event["endDate"] != null) {
            val endDate = DateParser.parse(event["endDate"].toString())
            dates = DateParserResult(listOf(DatePair(dates.dates[0].startDate, endDate.dates[0].startDate)))
        }

        return structuredEvent(event["name"].toString(), dates) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event["url"].toString()))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event["url"].toString()))
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, event["locationName"].toString())
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Wien")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse(baseUrl))
            withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.OTHER)
        }
    }
}
