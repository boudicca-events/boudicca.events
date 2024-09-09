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
import java.io.StringReader
import java.net.URI
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ArenaWienCollector : TwoStepEventCollector<ArenaWienCollector.HalfEvent>("arenawien") {

    private val fetcher = Fetcher()
    private val jsonParser = Parser.default()

    override fun getAllUnparsedEvents(): List<HalfEvent> {
        val halfEvents = mutableListOf<HalfEvent>()
        val parsedFirstSite = getProgramList(0)
        halfEvents.addAll(getAllUrls(parsedFirstSite))
        val maxPage = parsedFirstSite.int("maxPage")!!
        for (i in 1..maxPage) {
            halfEvents.addAll(getAllUrls(getProgramList(i)))
        }

        return halfEvents
    }

    override fun parseStructuredEvent(event: HalfEvent): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event.url))

        return StructuredEvent
            .builder(event.title!!, parseDate(event.dateBegin!!))
            .withProperty(
                SemanticKeys.ENDDATE_PROPERTY,
                if (!event.dateEnd.isNullOrBlank()) parseDate(event.dateEnd) else null
            )
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event.url))
            .withProperty(SemanticKeys.TYPE_PROPERTY, "concert")
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.suite_VAdescr").text())
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, getPictureUrl(eventSite))
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Arena Wien")
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event.url))
            .build()
    }

    private fun getPictureUrl(eventSite: Document): URI? {
        val img = eventSite.select("div.suite_imageContainer img")
        return if (!img.isEmpty()) {
            UrlUtils.parse("https://arena.wien/" + img.first()!!.attr("src"))
        } else {
            null
        }
    }

    private fun parseDate(dateText: String): OffsetDateTime {
        return LocalDateTime.parse(dateText, DateTimeFormatter.ISO_DATE_TIME)
            .atZone(ZoneId.of("Europe/Vienna"))
            .toOffsetDateTime()
    }

    private fun getProgramList(i: Int): JsonObject {
        return jsonParser.parse(StringReader(fetcher.fetchUrl(getAjaxUrl(i)))) as JsonObject
    }

    private fun getAllUrls(jsonObject: JsonObject): Collection<HalfEvent> {
        return jsonObject.array<JsonObject>("concerts")!!.map {
            HalfEvent(
                it.string("DetailUrl")!!,
                it.string("DateBegin"),
                it.string("DateEnd"),
                it.string("Location"),
                it.string("Title"),
            )
        }
    }

    private fun getAjaxUrl(page: Int): String {
        return "https://arena.wien/DesktopModules/WebAPI/API/Event/Search?searchTerm=&day=1&month=-1&year=-1&page=${page}&pageSize=20&eventCategory=-1&abonnement=-1&cultureCode=de-AT&locationId=0"
    }

    data class HalfEvent(
        val url: String,
        val dateBegin: String?,
        val dateEnd: String?,
        val location: String?,
        val title: String?,
    )
}
