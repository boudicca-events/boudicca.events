package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class BurgClamCollector : TwoStepEventCollector<String>("burgclam") {
    private val fetcher = Fetcher()

    override fun getAllUnparsedEvents(): List<String> {

        val document = Jsoup.parse(fetcher.fetchUrl("https://clamlive.at/shows/#/"))
        return document
            .select("section.eventCollection a")
            .map { it.attr("href") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("h1.eventTitle").text()
        val dateText = eventSite.select("div.eventDate").text()
        val startDate = parseDate(dateText)

        var description = eventSite.select("section.eventSingle__description").text()
        val lineupElement = eventSite.select("li.lineupList__item")
        if (lineupElement.isNotEmpty()) {
            val lineup = "Line-up:\n" + lineupElement.map{it.text()}.joinToString("\n") + "\n"
            description = lineup + description
        }

        return StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            .withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                UrlUtils.parse(eventSite.select("eventSingle__headerBanner img").attr("src"))
            )
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description)
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Burg Clam")
            .build()
    }

    private fun parseDate(dateText: String): OffsetDateTime {
        var fixedDateText = dateText.replace("Jänner", "Januar")
            .replace("JULI", "Juli") //why the heck is this case sensitive
        if (fixedDateText.contains(", ")) {
            fixedDateText = fixedDateText.split(", ")[1]
        }

        val date = LocalDate.parse(
            fixedDateText,
            DateTimeFormatter.ofPattern("d. LLL uuuu").withLocale(Locale.GERMAN)
        )

        return date.atStartOfDay().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
