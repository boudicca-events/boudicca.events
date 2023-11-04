package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.jsoup.Jsoup
import java.io.StringReader
import java.time.LocalDateTime
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

    override fun parseEvent(event: HalfEvent): Event {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event.url))

        val startDate =
            LocalDateTime.parse(event.dateBegin, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.of("Europe/Vienna"))
                .toOffsetDateTime()

        val data = mutableMapOf<String, String>()
        if (!event.dateEnd.isNullOrBlank()) {
            data[SemanticKeys.ENDDATE] = event.dateEnd
        }
        data[SemanticKeys.URL] = event.url
        data[SemanticKeys.TYPE] = "concert"
        data[SemanticKeys.DESCRIPTION] = eventSite.select("div.suite_VAdescr").text()

        val img = eventSite.select("div.suite_imageContainer img")
        if (!img.isEmpty()) {
            data[SemanticKeys.PICTUREURL] = "https://arena.wien/" + img.first()!!.attr("src")
        }

        data[SemanticKeys.LOCATION_NAME] = "Arena Wien"

        return Event(event.title!!, startDate, data)
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
