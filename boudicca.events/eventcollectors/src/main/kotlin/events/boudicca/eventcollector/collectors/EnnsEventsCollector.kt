package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.annotations.BoudiccaEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventcollector.util.structuredEvent
import base.boudicca.dateparser.dateparser.DatePair
import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.StringReader

@BoudiccaEventCollector("ennsevents")
class EnnsEventsCollector : TwoStepEventCollector<JsonObject>("ennsevents") {
    private val baseUrl = "https://erlebe.enns.at/"

    override fun getAllUnparsedEvents(): List<JsonObject> {
        val fetcher = FetcherFactory.newFetcher()
        val unparsedJson =
            fetcher.fetchUrl("${baseUrl}snpapi/search/v2?rows=400&start=0&q=&scope=ennsportal&sort=datum&type=EVENT")
        val jsonObject = Parser.default().parse(StringReader(unparsedJson)) as JsonObject
        return jsonObject.array<JsonObject>("items")!!.toList()
    }

    override fun parseMultipleStructuredEvents(event: JsonObject): List<StructuredEvent?>? {
        val name = event.string("title")!!
        val dates = parseDates(event)
        val url = "${baseUrl}events/e/" + event.string("id")
        val description = (event.string("subtitle") + "\n" + event.string("description")).trim()

        val locationUrl = UrlUtils.parse(event.string("website"))

        var city = event.string("city")
        if (city != null && city.contains("-")) { // fix city "4470 ENNS - Enns"
            city = city.split("-")[1].trim()
        }
        val street = event.string("addressLine")
        val zipCode = event.string("zipCode")
        var address: String? = null
        if (city != null && street != null && zipCode != null) {
            address = "$street, $zipCode $city"
        }
        val lat = event.double("lat")
        val lon = event.double("lon")

        val pictureUrl = UrlUtils.parse("${baseUrl}uploads/images/thumbs_square/", event.string("picture"))

        return structuredEvent(name, dates) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, locationUrl)
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, city)
            withProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY, address)
            withProperty(SemanticKeys.LOCATION_COORDINATES_LAT_PROPERTY, lat)
            withProperty(SemanticKeys.LOCATION_COORDINATES_LON_PROPERTY, lon)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "Erlebe Enns")
        }
    }

    private fun parseDates(element: JsonObject): DateParserResult {
        var startDate = DateParser.parse(element.string("startDate")!!)
        if (element.string("endDate") != null) {
            val endDate = DateParser.parse(element.string("endDate")!!)
            startDate = DateParserResult(listOf(DatePair(startDate.dates[0].startDate, endDate.dates[0].startDate)))
        }
        return startDate
    }
}
