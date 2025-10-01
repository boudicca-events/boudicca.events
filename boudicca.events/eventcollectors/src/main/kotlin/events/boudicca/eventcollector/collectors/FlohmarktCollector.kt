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

        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "flohmaerkte/oberoesterreich"))
        val eventUrls = document.select("div.terminTitel")
            .mapNotNull { it.select("a").first()?.attr("href") }

        // TODO: other states
        // TODO: other pages?
//        val eventUrls = mutableListOf<String>()
//
//        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "termine/"))
//
//        val stateUrls = document.select(".navInhalt:nth-child(2) li:not(.navsub) a")
//            .mapNotNull { it.select("a").first()?.attr("href") }
//
//        stateUrls.forEach {
//            stateUrl ->
//            eventUrls.addAll(Jsoup.parse(fetcher.fetchUrl(baseUrl + stateUrl.replace("../", "")))
//                .select("div.terminTitel")
//                .mapNotNull { it.select("a").first()?.attr("href") })
//        }

        return eventUrls
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val document = Jsoup.parse(fetcher.fetchUrl(event))
        val name = document.select("div.terminTitel").text()

        val detailNodes = document.select("div#termineDetail").first()!!.childNodes()
            .filter { node -> node.nodeValue().isNotBlank() && !node.nameIs("br")}

        // adding up all the text lines which are in the description with and without any tags
        val description = StringBuilder()
        detailNodes.forEach { description.append(it.nodeValue().trim()).append("\n") }

        val cityRegex = """in\s\d{4}\s(?<city>[\w\s]+)""".toRegex()
        val cityMatchResult = cityRegex.find(name)
        if (cityMatchResult == null) {
            throw Exception("")
        }
        val city = cityMatchResult.groups["city"]!!.value.trimEnd()

//        val dateNode = detailNodes.first().nodeValue().trim()
        val dateRegex = """(?<startDate>\d{1,2}\.\s+\w*\s+\d{2,4})\D*(?<endDate>\d{1,2}\.\s+\w*\s+\d{2,4})?""".toRegex()
        val dateMatchResult = dateRegex.find(description)
        val startDateString = dateMatchResult?.groups["startDate"]!!.value.trim()
        val endDateString = dateMatchResult.groups["endDate"]?.value

//        val locationAndTime = detailNodes[2].nodeValue().trim()
        val timeRegex = """(?<address>[^\d,]+\.?\s?\d+),.*?(?<start>\d{1,2}:?\d{0,2})-(?<end>\d{1,2}:?\d{0,2})[\sUhr]*""".toRegex()
        val timeMatchResult = timeRegex.find(description)
        val address = timeMatchResult?.groups["address"]!!.value.trim()
        val startTimeString = timeMatchResult.groups["start"]!!.value.trim()
        val endTimeString = timeMatchResult.groups["end"]!!.value.trim()

        val startTime = parseTime(startTimeString)
        val startDate = LocalDate.parse(startDateString.trim().replace("  ", " "),
            DateTimeFormatter.ofPattern("d. MMMM yyyy").withLocale(Locale.GERMAN))
            .atTime(startTime).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()

        var endDate: OffsetDateTime = OffsetDateTime.now()
        var endDateWasFound = false
        if (endDateString != null && endTimeString.isNotBlank()) {
            val endTime = parseTime(endTimeString)
            endDate = LocalDate.parse(
                endDateString.trim().replace("  ", " "),
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

    fun parseTime(timeToParse: String) : LocalTime {
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
