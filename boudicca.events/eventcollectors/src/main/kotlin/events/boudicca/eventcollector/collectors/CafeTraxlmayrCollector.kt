package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class CafeTraxlmayrCollector : TwoStepEventCollector<Element>("cafetraxlmayr") {
    private val LOG = LoggerFactory.getLogger(this::class.java)
    private val baseUrl = "https://www.cafe-traxlmayr.at/konzerte/"

    override fun getAllUnparsedEvents(): List<Element> {
        val fetcher = Fetcher()

        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))
        return document
            .select("div.fusion-text a.fusion-modal-text-link")
            .map { it.attr("data-target") }
            .map { document.select(it).first()!! }
    }

    override fun parseMultipleEvents(event: Element): List<Event?> {
        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = baseUrl //they do not have directlinks

        data[SemanticKeys.LOCATION_NAME] = "Café Traxlmayr"
        data[SemanticKeys.LOCATION_URL] = "https://www.cafe-traxlmayr.at/"
        data[SemanticKeys.LOCATION_CITY] = "Linz"
        data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!

        val pictureUrl = event.select("img").attr("src")
        if (pictureUrl.isNotBlank()) {
            data[SemanticKeys.PICTUREURL] = pictureUrl
        }

        val title = event.select("h3").text()
        return if (title.contains("|")) {
            parseConcert(title, event, data)
        } else if (title == "Die nächsten Lesungen im Café Traxlmayr") {
            parseLesungen(event, data)
        } else {
            LOG.info("cannot parse event: $event")
            emptyList()
        }
    }

    private fun parseConcert(title: String, event: Element, data: MutableMap<String, String>): List<Event?> {
        val split = title.split(" | ")
        val name = split[0].trim()
        val bodyLines = event.select(".modal-body p strong").textNodes()

        val fullDateText = bodyLines.first { it.text().contains("Uhr") }

        val startDate = parseDate(fullDateText.text().trim())

        data[SemanticKeys.DESCRIPTION] = event.select(".modal-body").text()
        return listOf(Event(name, startDate, data))
    }

    private fun parseLesungen(event: Element, data: MutableMap<String, String>): List<Event?> {
        val lesungen = mutableListOf<Event>()
        var currentName: String? = null
        var currentDescription = ""
        var currentDate: OffsetDateTime? = null

        for (line in event.select(".modal-body p ").eachText().filter { it.isNotBlank() }) {
            if (line.contains("_______")) {
                val currentData = data.toMutableMap()
                currentData[SemanticKeys.DESCRIPTION] = currentDescription
                lesungen.add(Event(currentName!!, currentDate!!, currentData))
                currentName = null
                currentDescription = ""
                currentDate = null
            } else {
                if (currentName == null) {
                    currentName = line
                }
                currentDescription += line + "\n"
                if (line.contains("Uhr")) {
                    currentDate = parseDate(line)
                }
            }
        }

        val currentData = data.toMutableMap()
        currentData[SemanticKeys.DESCRIPTION] = currentDescription
        lesungen.add(Event(currentName!!, currentDate!!, currentData))

        return lesungen
    }

    private fun parseDate(fullDateText: String): OffsetDateTime {
        val split = fullDateText.split(',', '|')
        val dateText = split[1].trim()
        val timeText = split[2].trim()

        val date = LocalDate.parse(
            dateText.replace("Jänner", "Januar"),
            DateTimeFormatter.ofPattern("d. LLLL uuuu").withLocale(Locale.GERMAN)
        )
        val time = LocalTime.parse(
            timeText,
            DateTimeFormatter.ofPattern("kk.mm 'Uhr'").withLocale(Locale.GERMAN)
        )

        return date.atTime(time).atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
