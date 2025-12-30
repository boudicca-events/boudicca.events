package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.lookup
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.StringReader
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["kupfticket"])
class KupfTicketCollector : TwoStepEventCollector<String>("kupfticket") {
    private val baseUrl = "https://kupfticket.com/events"
    private val fetcher = FetcherFactory.newFetcher()

    override fun getAllUnparsedEvents(): List<String> {
        val eventSlugs = mutableSetOf<String>()
        var currentUrl: String? = baseUrl

        while (currentUrl != null) {
            val document = Jsoup.parse(fetcher.fetchUrl(currentUrl))
            val nextData = document.select("body script#__NEXT_DATA__").first()!!.data()
            val jsonObject = Parser.default().parse(StringReader(nextData)) as JsonObject

            eventSlugs.addAll(jsonObject.lookup("props.pageProps.events.edges.node.slug"))

            val hasNext = jsonObject.lookup<Boolean>("props.pageProps.events.pageInfo.hasNextPage").first()
            currentUrl =
                if (hasNext) {
                    "$baseUrl/cursor/after/" +
                        jsonObject.lookup<String>("props.pageProps.events.pageInfo.endCursor").first()
                } else {
                    null
                }
        }

        return eventSlugs.toList()
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl("$baseUrl/$event"))
        val nextData = eventSite.select("body script#__NEXT_DATA__").first()!!.data()
        val jsonObject = Parser.default().parse(StringReader(nextData)) as JsonObject
        val eventJson = jsonObject.lookup<JsonObject>("props.pageProps.event").first()

        val name = eventJson["title"] as String
        val description = htmlToCleanTextWithLineBreaks(eventJson["description"].toString())
        val url = "$baseUrl/" + (eventJson["slug"] as String)
        val location = eventJson.lookup<String>("location.title").first()
        val startDate = parseDate(eventJson.lookup<String>("date.start").first())
        val endDate = parseDate(eventJson.lookup<String>("date.end").first())

        val pictureUrl = eventJson.lookup<String>("image.src").first()
        val pictureAlt = eventJson.lookup<String>("image.description").first().trim()
        var pictureCopyright = eventJson.lookup<String>("image.credits").first().ifBlank { "KupfTicket" }
        pictureCopyright = pictureCopyright.replace(Regex("""^\s?c\s|\(c\)|©|@|:"""), "").trim()

        val (locationName, locationAddress) = splitLocation(location)
        val cityRegex = """.*,\s\d{4,5}\s(?<city>\D*),""".toRegex()
        val locationCity = cityRegex.find(location)?.groupValues?.last()

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, locationName)
            withProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY, locationAddress)
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, locationCity)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(pictureUrl))
            withProperty(SemanticKeys.PICTURE_ALT_TEXT_PROPERTY, pictureAlt)
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, pictureCopyright)
            withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
        }
    }

    private fun splitLocation(location: String): Pair<String, String?> =
        if (location.count { it == ',' } > 2) { // probably location name + address
            val split = location.split(",", limit = 2)
            Pair(split[0].trim(), split[1].trim())
        } else {
            Pair(location, null)
        }

    private fun parseDate(date: String): OffsetDateTime = OffsetDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)

    private fun htmlToCleanTextWithLineBreaks(html: String): String {
        val doc = Jsoup.parse(html)
        doc.select("br, p, div, li, tr, h1, h2, h3, h4, h5, h6").append("\\n")
        val cleaned = Jsoup.clean(doc.html(), "", Safelist.none(), Document.OutputSettings().prettyPrint(false))
        return cleaned.replace("\\n", "\n").replace("\\s+".toRegex()) { it.value[0].toString() }.trim()
    }
}
