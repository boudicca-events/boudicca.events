package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MetalCornerCollector : TwoStepEventCollector<Triple<String, String, Document>>("metalcorner") {

  private val baseUrl = "https://www.escape-metalcorner.at/"

  override fun getAllUnparsedEvents(): List<Triple<String, String, Document>> {
    val fetcher = Fetcher()

    val events = mutableListOf<Triple<String, String, Document>>()
    val eventUrls = mutableListOf<Pair<String, String>>()

    val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "de/events"))
    document.select("div#content > div#events .event")
      .forEach {
        eventUrls.add(Pair(it.select(".head").text(), it.select("a.overlay").attr("href")))
      }

    eventUrls.forEach {
      val url = baseUrl + it.second.substring(2)
      val eventType = it.first
      events.add(Triple(eventType, url, Jsoup.parse(fetcher.fetchUrl(url))))
    }

    return events
  }

  override fun parseEvent(event: Triple<String, String, Document>): Event {
    val (eventType, url, doc) = event
    val data = mutableMapOf<String, String>()
    data[SemanticKeys.URL] = url
    data[SemanticKeys.TYPE] = eventType
    data[SemanticKeys.NAME] = "Escape - " + doc.select("div#content h1").text()
    data[SemanticKeys.PICTURE_URL] = doc.select("div#content img").attr("src")
    data[SemanticKeys.PICTURE_ALT_TEXT] = doc.select("div#content img").attr("alt")
    data[SemanticKeys.SOURCES] = data[SemanticKeys.URL]!!
    data[SemanticKeys.LOCATION_NAME] = "Escape Metalcorner"
    data[SemanticKeys.LOCATION_CITY] = "Wien"
    data[SemanticKeys.LOCATION_ADDRESS] = "Escape Metalcorner, Neustiftgasse 116-118, 1070 Wien"
    data[SemanticKeys.LOCATION_URL] = "https://www.escape-metalcorner.at/"
    data[SemanticKeys.REGISTRATION] = "ticket"
    data[SemanticKeys.CATEGORY] = EventCategory.MUSIC.name

    val formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy HH:mm", Locale.GERMAN)
    val zoneId = TimeZone.getTimeZone("Europe/Vienna").toZoneId()
    val eventStartDate = LocalDateTime.parse(doc.select("div#content h2").text(), formatter)
      .atZone(zoneId).toOffsetDateTime()

    return Event(name = "my event", eventStartDate, data)
  }
}
