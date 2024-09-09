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
            .select("a.av-screen-reader-only")
            .map { it.attr("href") }
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val headLines = eventSite.select("h3.av-special-heading-tag")

        var dateText = headLines[0].text()
        if (dateText == "SOLD OUT") {
            dateText = eventSite.select("div.av-subheading.av-subheading_below")[0].text()
        }
        val startDate = parseDate(dateText)

        val name = if (headLines.size >= 2) {
            headLines[1].text()
        } else {
            eventSite.select("div.av-subheading").text()
        }

        return StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            .withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                UrlUtils.parse(eventSite.select("img.avia_image").attr("src"))
            )
            .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, eventSite.select("div.av-subheading").text())
            .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, "Burg Clam")
            .build()
    }

    private fun parseDate(dateText: String): OffsetDateTime {
        var fixedDateText = dateText.replace("Jänner", "Januar")
            .replace("JULI", "Juli") //why the heck is this case sensititve
        if (fixedDateText.contains(", ")) {
            fixedDateText = fixedDateText.split(", ")[1]
        }

        val date = LocalDate.parse(
            fixedDateText,
            DateTimeFormatter.ofPattern("d. LLLL uuuu").withLocale(Locale.GERMAN)
        )

        return date.atStartOfDay().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

}
