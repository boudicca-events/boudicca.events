package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.Event
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class InnovationsHauptplatzCodingWeeksCollector : TwoStepEventCollector<Element>("innovationshauptplatzcodingweeks") {

    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<Element> {
        val document = Jsoup.parse(fetcher.fetchUrl("https://innovation.linz.at/de/aktuelle-projekte/coding-weeks/"))
        return document.select("div#listView div.event")
            .asSequence()
            .map { it.parent()!! }
            .filter { it.attr("href").isNotBlank() }
            .map { it.outerHtml() }
            .distinct() //we need to deduplicate it because the site has it entered multiple times for different target audiences
            .map { Jsoup.parse(it) }
            .toList()
    }

    override fun parseEvent(event: Element): Event {
        val name = event.select("div.title").text()
        val startDate = parseDate(event)

        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = event.select("a").attr("href")
        data[SemanticKeys.TYPE] = "technology"
        data[SemanticKeys.PICTUREURL] = "https://innovation.linz.at" + event.select("img").attr("src")

        return Event(name, startDate, data)
    }

    private fun parseDate(element: Element): OffsetDateTime {
        val dateString = element.select("div.date").text()
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.LL.uuuu"))
            .atStartOfDay()
            .atZone(ZoneId.of("Europe/Vienna"))
            .toOffsetDateTime()
    }

}
