package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.jsoup.Jsoup
import java.io.StringReader
import java.time.OffsetDateTime
import java.time.ZoneId

class RoedaCollector : TwoStepEventCollector<JsonObject>("roeda") {

    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<JsonObject> {
        //uh boy, this is ugly
        val pageSource = fetcher.fetchUrl("https://xn--rda-sna.at")
        val jsonLine = pageSource
            .lines()
            .single { it.startsWith("var EventsSchedule_1 = ") }
        val jsonString = jsonLine.removePrefix("var EventsSchedule_1 = ").removeSuffix(";")
        val json = Parser.default().parse(StringReader(jsonString)) as JsonObject
        return json.array<JsonObject>("feed")?.toList() ?: emptyList()
    }

    override fun parseStructuredEvent(event: JsonObject): StructuredEvent {

        val name = event.string("title")!!

        val description = htmlToText(event.string("excerpt")!!)

        //the manage to have a timestamp with timezone offset, but the offset is always wrong and 0 -.-
        val start = OffsetDateTime.parse(event.string("start")!!).fixTimeZone()
        val end = OffsetDateTime.parse(event.string("end")!!).fixTimeZone()

        val pictureUrl = event.string("thumbnail")!!
        val types = event.obj("terms")!!.array<JsonObject>("wcs_type")!!
        val tags = types.map { it.string("name") }.toList().filterNotNull()

        return structuredEvent(name, start) {
            withProperty(SemanticKeys.ENDDATE_PROPERTY, end)
            withProperty(SemanticKeys.TYPE_PROPERTY, tags.first())
            withProperty(SemanticKeys.TAGS_PROPERTY, tags)
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse("https://röda.at/"))
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, pictureUrl)
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "kulturverein röda")
            withProperty(SemanticKeys.LOCATION_URL_PROPERTY, UrlUtils.parse("https://röda.at/"))
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, "Steyr")
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf("https://röda.at/"))
        }
    }

    private fun htmlToText(html: String): String {
        return Jsoup.parse(html).text()
    }

}

private fun OffsetDateTime.fixTimeZone(): OffsetDateTime {
    return this.toLocalDateTime().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
}
