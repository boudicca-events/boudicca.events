package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class FemaleCoderCollector : TwoStepEventCollector<String>("femalecoder") {

  private val LOG = LoggerFactory.getLogger(this::class.java)
  private val fetcher = Fetcher()
  private val baseUrl = "https://female-coders.at/"

  override fun getAllUnparsedEvents(): List<String> {
    val eventUrls = mutableListOf<String>()

    val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))

    document.select("div.et_pb_section_3 .event_details")
      .forEach {
        eventUrls.add(it.select("a").first().attr("href"))
      }

    return eventUrls
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

    val date = document.select("div.details p").first()?.text()
    val startTime = document.select("div.details p").last()?.text()?.substring(0, 8)!!

    val currentYear = LocalDate.now().year

    val localDate = LocalDate.parse("$date $currentYear", DateTimeFormatter.ofPattern("MMMM dd yyyy", Locale.ENGLISH))
    val localStartTime = LocalTime.parse(startTime.uppercase(Locale.getDefault()), DateTimeFormatter.ofPattern("hh:mm a"))

    val offsetDateTime = localDate.atTime(localStartTime)
      .atZone(ZoneId.of("Europe/Vienna"))
      .toOffsetDateTime()

    data[SemanticKeys.PICTURE_URL] = document.select("div.container > div#content-area img").attr("href")
    data[SemanticKeys.TYPE] = "technology"
    data[SemanticKeys.CATEGORY] = EventCategory.TECH.name
    data[SemanticKeys.TAGS] = listOf("Study Group", "Coding", "Mentorship", "TechCommunity", "Socializing", "Networking").toString()

    data[SemanticKeys.LOCATION_NAME] = document.select("div.venue > p").first()?.text()!!
//    data[SemanticKeys.LOCATION_CITY] =

    return Event(name, offsetDateTime, data)
  }
}
