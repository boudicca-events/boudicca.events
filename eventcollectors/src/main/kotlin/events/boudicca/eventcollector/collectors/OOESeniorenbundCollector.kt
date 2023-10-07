package events.boudicca.eventcollector.collectors

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.Fetcher
import events.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * the actual eventlist on https://ooesb.at/veranstaltungen is an iframe pointing to https://servicebroker.media-data.at/overview.html?key=QVKSBOOE so we parse that
 */
class OOESeniorenbundCollector : TwoStepEventCollector<Pair<Document, String>>("ooesb") {

    override fun getAllUnparsedEvents(): List<Pair<Document, String>> {
        val fetcher = Fetcher(500) //server seems really slow...
        val document = Jsoup.parse(fetcher.fetchUrl("https://servicebroker.media-data.at/overview.html?key=QVKSBOOE"))

        return document.select("a.link-detail")
            .toList()
            .map { "https://servicebroker.media-data.at/" + it.attr("href") }
            .map { Pair(Jsoup.parse(fetcher.fetchUrl(it)), it) }
    }

    override fun parseEvent(pair: Pair<Document, String>): Event {
        val (event, url) = pair
        val data = mutableMapOf<String, String>()

        val name = event.select("div.title>p").text()
        val (startDate, endDate) = getDates(event)
        val description = event.select("div.subtitle>p").text()

        data[SemanticKeys.URL] = cleanupUrl(url)
        data[SemanticKeys.LOCATION_NAME] = event.select("div.venue").text() //TODO location name and city here are not seperated at all -.-
        if (endDate != null) {
            data[SemanticKeys.ENDDATE] = endDate.format(DateTimeFormatter.ISO_DATE_TIME)
        }
        if (description.isNotBlank()) {
            data[SemanticKeys.DESCRIPTION] = description
        }

        return Event(name, startDate.toOffsetDateTime(), data)
    }

    private fun cleanupUrl(url: String): String {
        //https://servicebroker.media-data.at/detail.html;jsessionid=B20D66D14ABACD0C9357ECC77CA10E48?evkey=11774&resize=true&key=QVKSBOOE

        val sessionIdPattern = Pattern.compile("\\;jsessionid\\=[\\d\\w]+\\?")
        val matcher = sessionIdPattern.matcher(url)

        return if (matcher.find()) {
            url.replace(matcher.group(0), "?")
        } else {
            url
        }
    }

    private fun getDates(event: Document): Pair<ZonedDateTime, ZonedDateTime?> {
        val dateString = event.select("div.date>p").text()

        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu")
        val timeFormatter = DateTimeFormatter.ofPattern("kk:mm")

        val datePattern = "(\\d{2}\\.\\d{2}.\\d{4})"
        val timePattern = "(\\d{2}\\:\\d{2})"
        val dateFromTillPattern =
            Pattern.compile("^$datePattern von $timePattern bis $timePattern Uhr$")
        val dateFromDateTillPattern =
            Pattern.compile("^von $datePattern $timePattern bis $datePattern $timePattern Uhr$")
        val dateFromDateTillWithoutTimePattern =
            Pattern.compile("^von $datePattern bis $datePattern$")


        val dateFromTillMatcher = dateFromTillPattern.matcher(dateString)
        val dateFromDateTillMatcher = dateFromDateTillPattern.matcher(dateString)
        val dateFromDateTillWithoutTimeMatcher = dateFromDateTillWithoutTimePattern.matcher(dateString)

        if (dateFromTillMatcher.matches()) {
            val localDate = LocalDate.parse(dateFromTillMatcher.group(1), dateFormatter)
            return Pair(
                localDate.atTime(LocalTime.parse(dateFromTillMatcher.group(2), timeFormatter)).atZone(ZoneId.of("Europe/Vienna")),
                localDate.atTime(LocalTime.parse(dateFromTillMatcher.group(3), timeFormatter)).atZone(ZoneId.of("Europe/Vienna"))
            )
        } else if (dateFromDateTillMatcher.matches()) {
            val startLocalDate = LocalDate.parse(dateFromDateTillMatcher.group(1), dateFormatter)
            val endLocalDate = LocalDate.parse(dateFromDateTillMatcher.group(3), dateFormatter)
            return Pair(
                startLocalDate.atTime(LocalTime.parse(dateFromDateTillMatcher.group(2), timeFormatter))
                    .atZone(ZoneId.of("Europe/Vienna")),
                endLocalDate.atTime(LocalTime.parse(dateFromDateTillMatcher.group(4), timeFormatter))
                    .atZone(ZoneId.of("Europe/Vienna"))
            )
        } else if (dateFromDateTillWithoutTimeMatcher.matches()) {
            val startLocalDate = LocalDate.parse(dateFromDateTillWithoutTimeMatcher.group(1), dateFormatter)
            val endLocalDate = LocalDate.parse(dateFromDateTillWithoutTimeMatcher.group(2), dateFormatter)
            return Pair(
                startLocalDate.atTime(0, 0, 0).atZone(ZoneId.of("Europe/Vienna")),
                endLocalDate.atTime(0, 0, 0).atZone(ZoneId.of("Europe/Vienna"))
            )
        } else {
            return Pair(
                LocalDate.parse(dateString, dateFormatter).atTime(0, 0, 0).atZone(ZoneId.of("Europe/Vienna")),
                null
            )
        }
    }
}
