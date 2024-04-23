package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MetalCornerCollector : TwoStepEventCollector<Pair<String, String>>("metalcorner") {

  private val fetcher = Fetcher()
  private val baseUrl = "https://www.escape-metalcorner.at/"

  override fun getAllUnparsedEvents(): List<Pair<String, String>> {
    val eventUrls = mutableListOf<Pair<String, String>>()

    val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "de/events"))
    document.select("div#content > div#events .event")
      .forEach {
        eventUrls.add(Pair(it.select(".head").text(),
          it.select("a.overlay").attr("href").substring(2)))
      }

    return eventUrls
  }

  override fun parseEvent(event: Pair<String, String>): Event {
    val (eventType, url) = event
    val data = mutableMapOf<String, String>()

    val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + url))

    val name = "Escape - " + document.select("div#content h1").text()

    val description = document.select("div#content p").text()
    if (description.isNotBlank()) {
      data[SemanticKeys.DESCRIPTION] = description
    }

    data[SemanticKeys.URL] = baseUrl + url
    data[SemanticKeys.TYPE] = eventType
    data[SemanticKeys.PICTURE_URL] = document.select("div#content img").attr("src")
    data[SemanticKeys.PICTURE_ALT_TEXT] = document.select("div#content img").attr("alt")
    data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!
    data[SemanticKeys.LOCATION_NAME] = "Escape Metalcorner"
    data[SemanticKeys.LOCATION_CITY] = "Wien"
    data[SemanticKeys.LOCATION_ADDRESS] = "Escape Metalcorner, Neustiftgasse 116-118, 1070 Wien"
    data[SemanticKeys.LOCATION_URL] = "https://www.escape-metalcorner.at/"
    data[SemanticKeys.REGISTRATION] = "ticket"
    data[SemanticKeys.CATEGORY] = EventCategory.MUSIC.name

    val formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy HH:mm", Locale.GERMAN)
    val zoneId = TimeZone.getTimeZone("Europe/Vienna").toZoneId()
    val eventStartDate = LocalDateTime.parse(document.select("div#content h2").text(), formatter)
      .atZone(zoneId).toOffsetDateTime()

    return Event(name, eventStartDate, data)
  }
}
