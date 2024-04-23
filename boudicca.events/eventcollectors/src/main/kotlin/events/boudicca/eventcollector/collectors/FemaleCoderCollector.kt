package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
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
        it.select("a").first()?.let { it1 -> eventUrls.add(it1.attr("href")) }
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

    paragraphs.forEach {
      if (it.parents().select("organizermain").isEmpty()) {
        description.append(it.text()).append("\n")
      }
    }
    data[SemanticKeys.DESCRIPTION] = description.toString()
    data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!


    val unformattedDate = document.select("h1.entry-title").text().substring(21)
    val unformattedTime = document.select("div.details p").last()?.text()?.substring(0, 8)

    val unformattedDateAndTime = unformattedTime + " - " + unformattedDate

    // TODO: fix it, can not parse!
    val formatter = DateTimeFormatter.ofPattern("HH:mm a - dd.MM.yyyy", Locale.GERMAN)
    val zoneId = TimeZone.getTimeZone("Europe/Vienna").toZoneId()
    val eventStartDate = LocalDateTime.parse(unformattedDateAndTime, formatter)
      .atZone(zoneId).toOffsetDateTime()

    return Event(name, eventStartDate, data)
  }
}
