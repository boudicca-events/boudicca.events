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

        val cityRegex = """in\s[\d\s]*(?<city>[^\d,]+),?\D*$""".toRegex()
        val cityMatchResult = cityRegex.findAll(name).map { it.groupValues[1] }.toList()
        val city = if(cityMatchResult.isNotEmpty()) cityMatchResult.last() else null

        val dateRegex = """(?<startDate>\d{1,2}\.\s+[\wä]*\s+\d{4})\D*(?<endDate>\d{1,2}\.\s+[\wä]*\s+\d{4})?""".toRegex()
        val dateMatchResult = dateRegex.find(description)
        val startDateString = dateMatchResult?.groups["startDate"]?.value?.trim()
        val endDateString = dateMatchResult?.groups["endDate"]?.value?.trim()

        val timeRegex = """\n(?<address>[^\d,]+\.?\s?\d+),.*?(?<start>\d{1,2}:?\d{0,2}):?-(?<end>\d{1,2}:?\d{0,2})[\sUhr]*""".toRegex()
        val timeMatchResult = timeRegex.find(description)
        val address = timeMatchResult?.groups["address"]?.value
        val startTimeString = timeMatchResult?.groups["start"]?.value
        val endTimeString = timeMatchResult?.groups["end"]?.value

        val startDate = parseDate(startDateString, startTimeString)
        val endDate = parseDate(endDateString, endTimeString)

        val imgSrc = document.select("div#termineDetail img").attr("src")

        return structuredEvent(name, startDate!!) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description.toString())
            withProperty(SemanticKeys.TYPE_PROPERTY, "others")
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, city)
            withProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY, address)
            if (endDate != null) withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            if (imgSrc.isNotBlank()) withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgSrc))
            withProperty(
                SemanticKeys.TAGS_PROPERTY,
                listOf("Flea market", "Thrifting", "Second Hand")
            )
        }
    }

    private fun parseDate(dateToParse: String?, timeToParse: String?) : OffsetDateTime?{
        if (dateToParse == null){
            return null
        }
        val time = parseTime(timeToParse)
        return LocalDate.parse(
            dateToParse.replace("  ", " ").replace("Jänner", "Januar"),
            DateTimeFormatter.ofPattern("d. MMMM yyyy").withLocale(Locale.GERMAN)
        ).atTime(time).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

    private fun parseTime(timeToParse: String?) : LocalTime {
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
