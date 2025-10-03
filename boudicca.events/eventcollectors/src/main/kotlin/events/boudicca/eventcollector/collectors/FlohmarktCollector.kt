package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class FlohmarktCollector : TwoStepEventCollector<String>("flohmarkt") {

    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.flohmarkt.at/"

    override fun getAllUnparsedEvents(): List<String> {

        val stateUrls = Jsoup.parse(fetcher.fetchUrl(baseUrl + "termine/"))
            .select(".navInhalt:nth-child(2) li:not(.navsub) a")
            .mapNotNull { it.select("a").first()?.attr("href") }

        val eventUrls = mutableListOf<String>()
        stateUrls.forEach {
            eventUrls.addAll(getEventUrlsOfEachState(it))
        }

        return eventUrls.distinct()
    }

    fun getEventUrlsOfEachState(stateUrl: String) : List<String> {
        val eventUrls = mutableListOf<String>()
        val pageUrls = Jsoup.parse(fetcher.fetchUrl(baseUrl + stateUrl.replace("../", "")))
            .select("div.weiterblaettern a")
            .mapNotNull { it.attr("href") }
        pageUrls.forEach { eventUrls.addAll(getEventUrlsOfEachPage(it)) }
        return eventUrls
    }

    fun getEventUrlsOfEachPage(statePageUrl: String) : List<String> {
        return Jsoup.parse(fetcher.fetchUrl(statePageUrl))
            .select("div.terminTitel a")
            .mapNotNull { it.attr("href") }
            .filter{ !it.contains("ß") } // exclude urls with ß, as they result in 404 error
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val document = Jsoup.parse(fetcher.fetchUrl(event))
        val name = document.select("div.terminTitel").text()

        val detailNodes = document.select("div#termineDetail").first()!!.childNodes()
            .filter { node -> node.nodeValue().isNotBlank() && !node.nameIs("br")}

        // adding up all the text lines which are in the description with and without any tags
        val description = StringBuilder()
        detailNodes.forEach { description.append(it.nodeValue().trim()).append("\n") }

        val cityRegex = """in\s\d{4,5}\s(?<city>[\w\s]+)""".toRegex()
        val cityMatchResult = cityRegex.find(name)
        val city = cityMatchResult?.groups["city"]?.value?.trim()

        val dateRegex = """(?<startDate>\d{1,2}\.\s+[\wä]*\s+\d{4})\D*(?<endDate>\d{1,2}\.\s+[\wä]*\s+\d{4})?""".toRegex()
        val dateMatchResult = dateRegex.find(description)
        val startDateString = dateMatchResult?.groups["startDate"]?.value?.trim()
        val endDateString = dateMatchResult?.groups["endDate"]?.value?.trim()

        val timeRegex = """(?<address>[^\d,]+\.?\s?\d+),.*?(?<start>\d{1,2}:?\d{0,2}):?-(?<end>\d{1,2}:?\d{0,2})[\sUhr]*""".toRegex()
        val timeMatchResult = timeRegex.find(description)
        val address = timeMatchResult?.groups["address"]?.value
        val startTimeString = timeMatchResult?.groups["start"]?.value
        val endTimeString = timeMatchResult?.groups["end"]?.value

        val startTime = parseTime(startTimeString)
        val startDate = LocalDate.parse(
            startDateString!!.replace("  ", " ").replace("Jänner", "Januar"),
            DateTimeFormatter.ofPattern("d. MMMM yyyy").withLocale(Locale.GERMAN))
            .atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()

        var endDate: OffsetDateTime = OffsetDateTime.now()
        var endDateWasFound = false
        if (endDateString != null && endTimeString != null) {
            val endTime = parseTime(endTimeString)
            endDate = LocalDate.parse(
                endDateString.replace("  ", " ").replace("Jänner", "Januar"),
                DateTimeFormatter.ofPattern("d. MMMM yyyy").withLocale(Locale.GERMAN)
                ).atTime(endTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
            endDateWasFound = true
        }

        val imgSrc = document.select("div#termineDetail img").attr("src")

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description.toString())
            withProperty(SemanticKeys.TYPE_PROPERTY, "others")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, city)
            withProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY, address)
            if (endDateWasFound) withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            if (imgSrc.isNotBlank()) withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgSrc))
            withProperty(
                SemanticKeys.TAGS_PROPERTY,
                listOf("Flea market", "Thrifting", "Second Hand")
            )
        }
    }

    fun parseTime(timeToParse: String?) : LocalTime {
        if (timeToParse == null){
            return LocalTime.MIN
        }
        val timeWithoutDelimiterRegex = """\d{3}""".toRegex()

        var timePattern = "H"
        if (timeToParse.contains(":")){
            timePattern = "H:mm"
        } else if (timeWithoutDelimiterRegex.containsMatchIn(timeToParse)) {
            timePattern = "Hmm"
        }

        return LocalTime.parse(
            timeToParse,
            DateTimeFormatter.ofPattern(timePattern).withLocale(Locale.GERMAN)
        )
    }
}
