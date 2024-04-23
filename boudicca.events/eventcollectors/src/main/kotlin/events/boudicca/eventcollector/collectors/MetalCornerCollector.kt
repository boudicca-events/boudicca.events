package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

class MetalCornerCollector : TwoStepEventCollector<Pair<Pair<String, String>, Document>>("metalcorner") {

  private val LOG = LoggerFactory.getLogger(this::class.java)
  private val baseUrl = "https://www.escape-metalcorner.at/"

  override fun getAllUnparsedEvents(): List<Pair<Pair<String, String>, Document>> {
    val fetcher = Fetcher()
    val events = mutableListOf<Pair<Pair<String, String>, Document>>()
    val eventUrls = mutableListOf<Pair<String, String>>()

    val document = Jsoup.parse(fetcher.fetchUrl(baseUrl + "de/events"))
    document.select("div#content > div#events .event")
      .forEach {
        eventUrls.add(Pair(it.select(".head").text(), it.select("a.overlay").attr("href")))
      }

    eventUrls.forEach {
      val url = baseUrl + it.second.substring(2)
      val eventTitle = it.first
      val document = Jsoup.parse(fetcher.fetchUrl(url))
      events.add(Pair(Pair(eventTitle, url), document))
    }

    return events
  }

  override fun parseEvent(event: Pair<Pair<String, String>, Document>): Event {
    val (eventUrl, doc) = event
    val (title, url) = eventUrl
    val data = mutableMapOf<String, String>()
    data[SemanticKeys.URL] = url

    LOG.info(title)
    LOG.info(doc.text())

    LOG.info(data.toString())

    val startDate = LocalDate.now(ZoneId.of("Europe/Vienna")) as OffsetDateTime

    return Event(name = "my event", startDate, data)
  }
}
