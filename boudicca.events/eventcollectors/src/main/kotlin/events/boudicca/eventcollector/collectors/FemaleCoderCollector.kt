package events.boudicca.eventcollector.collectors

import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.model.Event
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory

class FemaleCoderCollector : TwoStepEventCollector<String>("femalecoder") {

  private val LOG = LoggerFactory.getLogger(this::class.java)
  private val fetcher = Fetcher()
  private val baseUrl = "https://female-coders.at/"

  override fun getAllUnparsedEvents(): List<String>? {
    val eventUrls = mutableListOf<String>()

    val document = Jsoup.parse(fetcher.fetchUrl(baseUrl))

    document.select("div.et_pb_section_3 .event_details")
      .forEach {
        eventUrls.add(it.select("a").first().attr("href"))
      }

    return eventUrls
  }

  override fun parseEvent(event: String): Event? {
    TODO()
  }
}