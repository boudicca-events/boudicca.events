package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.format.UrlUtils
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.structuredEvent
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class FemaleCoderCollector : TwoStepEventCollector<String>("femalecoder") {
    private val fetcher = FetcherFactory.newFetcher()
    private val baseUrl = "https://female-coders.at/"

    override fun getAllUnparsedEvents(): List<String> {
        val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))

        val eventsUrls =
            document
                .select("div.et_pb_section_3 .event_details")
                .mapNotNull {
                    it.select("a").first()?.attr("href")
                }

        return eventsUrls
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val url = event

        val document = Jsoup.parse(fetcher.fetchUrl(event))

        val name = document.select("div.organizer p").text()

        val descriptionParent = document.select("div#content-area .entry-content")
        val paragraphs = descriptionParent.select("p")

        val description = StringBuilder()

        // adding up all the p tags which are in the description area and ignore does under "organizermain" locator
        paragraphs.forEach {
            if (it.parents().select("organizermain").isEmpty()) {
                description.append(it.text()).append("\n")
            }
        }

        // Wien, Le, AT, 1020
        // Wien, AT, 1120
        // Linz, In, AT, 4020
        // Hagenberg im Mühlkreis, AT, 4232
        val city =
            document
                .select("div.venue > p")
                .last()
                ?.text()!!
                .substringBefore(",", missingDelimiterValue = "Linz")

        val date = document.select("div.details p").first()?.text()
        val startTime =
            document
                .select("div.details p")
                .last()
                ?.text()
                ?.substring(0, 8)!!

        val currentYear = LocalDate.now().year

        val localDate =
            LocalDate.parse("$date $currentYear", DateTimeFormatter.ofPattern("MMMM dd yyyy", Locale.GERMAN))
        val localStartTime =
            LocalTime.parse(startTime.uppercase(Locale.ENGLISH), DateTimeFormatter.ofPattern("hh:mm a"))

        val startDate =
            localDate
                .atTime(localStartTime)
                .atZone(ZoneId.of("Europe/Vienna"))
                .toOffsetDateTime()

        return structuredEvent(name, startDate) {
            withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(url))
            withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(url))
            withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, description.toString())
            withProperty(
                SemanticKeys.PICTURE_URL_PROPERTY,
                UrlUtils.parse(document.select("div.container > div#content-area img").attr("src")),
            )
            withProperty(SemanticKeys.TYPE_PROPERTY, "technology")
            withProperty(
                SemanticKeys.TAGS_PROPERTY,
                listOf("Study Group", "Coding", "Mentorship", "TechCommunity", "Socializing", "Networking"),
            )
            withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.FREE)
            withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, document.select("div.venue > p").first()?.text()!!)
            withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, city)
        }
    }
}
