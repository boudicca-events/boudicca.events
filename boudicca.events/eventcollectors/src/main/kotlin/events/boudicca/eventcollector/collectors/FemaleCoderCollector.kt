package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class FemaleCoderCollector : TwoStepEventCollector<String>("femalecoder") {

  private val fetcher = Fetcher()
  private val baseUrl = "https://female-coders.at/"

  override fun getAllUnparsedEvents(): List<String> {
    val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))

    val eventsUrls = document.select("div.et_pb_section_3 .event_details")
      .map {
        it.select("a").first().attr("href")
      }

    return eventsUrls
  }

  override fun parseEvent(event: String): Event {
    val url = event
    val data = mutableMapOf<String, String>()

    val document = Jsoup.parse(fetcher.fetchUrl(event))

    val name = document.select("div.organizer p").text()

    data[SemanticKeys.URL] = url

    val descriptionParent = document.select("div#content-area .entry-content")
    val paragraphs = descriptionParent.select("p")

    val description = StringBuilder()

    // adding up all the p tags which are in the description area and ignore does under "organizermain" locator
    paragraphs.forEach {
      if (it.parents().select("organizermain").isEmpty()) {
        description.append(it.text()).append("\n")
      }
    }
    data[SemanticKeys.DESCRIPTION] = description.toString()

    data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!
    data[SemanticKeys.PICTURE_URL] = document.select("div.container > div#content-area img").attr("src")
    data[SemanticKeys.TYPE] = "technology"
    data[SemanticKeys.CATEGORY] = EventCategory.TECH.name
    data[SemanticKeys.TAGS] = listOf("Study Group", "Coding", "Mentorship", "TechCommunity", "Socializing", "Networking").toString()
    data[SemanticKeys.REGISTRATION] = "free"

    data[SemanticKeys.LOCATION_NAME] = document.select("div.venue > p").first()?.text()!!

    // Wien, Le, AT, 1020
    // Wien, AT, 1120
    // Linz, In, AT, 4020
    // Hagenberg im Mühlkreis, AT, 4232
    val city = document.select("div.venue > p").last()?.text()!!.substringBefore(",", missingDelimiterValue="Linz")
    data[SemanticKeys.LOCATION_CITY] = city

    val date = document.select("div.details p").first()?.text()
    val startTime = document.select("div.details p").last()?.text()?.substring(0, 8)!!

    val currentYear = LocalDate.now().year

    val localDate = LocalDate.parse("$date $currentYear", DateTimeFormatter.ofPattern("MMMM dd yyyy", Locale.ENGLISH))
    val localStartTime = LocalTime.parse(startTime.uppercase(Locale.ENGLISH), DateTimeFormatter.ofPattern("hh:mm a"))

    val offsetDateTime = localDate.atTime(localStartTime)
      .atZone(ZoneId.of("Europe/Vienna"))
      .toOffsetDateTime()

    return Event(name, offsetDateTime, data)
  }
}
