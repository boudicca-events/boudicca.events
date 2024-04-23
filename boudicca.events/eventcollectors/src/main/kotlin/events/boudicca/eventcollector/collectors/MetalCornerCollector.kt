package events.boudicca.eventcollector.collectors

import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.ZoneId

class MetalCornerCollector : TwoStepEventCollector<Pair<String, Document>>("metalcorner") {

  override fun getAllUnparsedEvents(): List<Pair<String, Document>> {
    val fetcher = Fetcher()
    val events = mutableListOf<Pair<String, Document>>()
    val eventUrls = mutableListOf<String>()
//    var date = LocalDate.now(ZoneId.of("Europe/Vienna"))

    val document = Jsoup.parse(fetcher.fetchUrl("https://www.escape-metalcorner.at/de/events"))
    document.select("div#events > div.events > a")
      .forEach { eventUrls.add(it.attr("href")) }

    eventUrls.forEach {
      events.add(Pair(it, Jsoup.parse(fetcher.fetchUrl(it))))
    }

    return events
  }


}