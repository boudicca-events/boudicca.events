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
import org.jsoup.Jsoup
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "boudicca.collector.enabled-collectors", name = ["flohmarkt"])
class FlohmarktCollector : TwoStepEventCollector<String>("flohmarkt") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://www.flohmarkt.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val stateUrls =
            Jsoup
                .parse(fetcher.fetchUrl(baseUrl + "termine/"))
                .select(".navInhalt:nth-child(2) li:not(.navsub) a")
                .mapNotNull { it.select("a").first()?.attr("href") }

        val eventUrls = mutableListOf<String>()
        stateUrls.forEach {
            eventUrls.addAll(getEventUrlsOfEachState(it))
        }

        return eventUrls.distinct()
    }

    fun getEventUrlsOfEachState(stateUrl: String): List<String> {
        val eventUrls = mutableListOf<String>()
        val pageUrls =
            Jsoup
                .parse(fetcher.fetchUrl(baseUrl + stateUrl.replace("../", "")))
                .select("div.weiterblaettern a")
                .mapNotNull { it.attr("href") }
        pageUrls.forEach { eventUrls.addAll(getEventUrlsOfEachPage(it)) }
        return eventUrls
    }

    fun getEventUrlsOfEachPage(statePageUrl: String): List<String> {
        return Jsoup
            .parse(fetcher.fetchUrl(statePageUrl))
            .select("div.terminTitel a")
            .mapNotNull { it.attr("href") }
            .filter { !it.contains("ß") } // exclude urls with ß, as they result in 404 error
    }

    override fun parseMultipleStructuredEvents(event: String): List<StructuredEvent?>? {
        val document = Jsoup.parse(fetcher.fetchUrl(event))
        val name = document.select("div.terminTitel").text()

        val detailNodes =
            document
                .select("div#termineDetail")
                .first()!!
                .childNodes()
                .filter { node -> node.nodeValue().isNotBlank() && !node.nameIs("br") }

        // adding up all the text lines which are in the description with and without any tags
        val description = StringBuilder()
        detailNodes.forEach { description.append(it.nodeValue().trim()).append("\n") }

        val cityRegex = """in\s[\d\s]*(?<city>[^\d,]+),?\D*$""".toRegex()
        val cityMatchResult = cityRegex.findAll(name).map { it.groupValues[1] }.toList()
        val city = if (cityMatchResult.isNotEmpty()) cityMatchResult.last() else null

        val dateRegex = """(?<startDate>\d{1,2}\.\s+[\wä]*\s+\d{4})\D*(?<endDate>\d{1,2}\.\s+[\wä]*\s+\d{4})?""".toRegex()
        val dateMatchResult = dateRegex.find(description)
        val startDateString = dateMatchResult?.groups["startDate"]?.value?.trim()
        val endDateString = dateMatchResult?.groups["endDate"]?.value?.trim()

        val timeRegex = """\n(?<address>\D*\d{0,3}).*,.*?(?<start>\d{1,2}:?\d{0,2}):?[–-](?<end>\d{1,2}:?\d{0,2})[\sUhr]*""".toRegex()
        val timeMatchResult = timeRegex.find(description)
        val address = timeMatchResult?.groups["address"]?.value
        val startTimeString = timeMatchResult?.groups["start"]?.value
        val endTimeString = timeMatchResult?.groups["end"]?.value

        var startDate = parseDate(startDateString, startTimeString)
        val endDate = parseDate(endDateString, endTimeString)
        if (endDate != null) {
            startDate = DateParserResult(listOf(DatePair(startDate!!.dates[0].startDate, endDate.dates[0].startDate)))
        }

        var imgSrc = document.select("div#termineDetail img").attr("src")
        if (imgSrc.isBlank()) {
            imgSrc = document.select("img.logo").attr("src")
        }

        return structuredEvent(name, startDate!!) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description.toString())
            withProperty(SemanticKeys.CATEGORY_PROPERTY, EventCategory.OTHER)
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, city)
            withProperty(SemanticKeys.LOCATION_ADDRESS_PROPERTY, address)
            withProperty(SemanticKeys.PICTURE_URL_PROPERTY, UrlUtils.parse(imgSrc))
            withProperty(SemanticKeys.PICTURE_COPYRIGHT_PROPERTY, "flohmarkt.at")
            withProperty(SemanticKeys.TAGS_PROPERTY, listOf("Flea market", "Thrifting", "Second Hand"))
        }
    }

    private fun parseDate(
        dateToParse: String?,
        timeToParseParam: String?,
    ): DateParserResult? {
        if (dateToParse == null) {
            return null
        }
        var timeToParse = timeToParseParam ?: ""
        val timeWithoutDelimiterRegex = """\d{3}""".toRegex()
        if (!timeToParse.contains(":") && timeWithoutDelimiterRegex.containsMatchIn(timeToParse)) {
            timeToParse = timeToParse[0] + ":" + timeToParse.substring(1) // make 330 parseable by converting it to 3:30
        }
        return DateParser.parse(dateToParse, timeToParse)
    }
}
