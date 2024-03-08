package base.boudicca.api.eventcollector.collectors

import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventdb.publisher.EventDbPublisherClient
import base.boudicca.model.Event

class BoudiccaCollector(private val url: String, private val name: String? = null) : EventCollector {

    override fun getName(): String {
        return name ?: "boudicca: $url"
    }

    override fun collectEvents(): List<Event> {
        Collections.startHttpCall(url)
        try {
            val events = EventDbPublisherClient(url).getAllEvents().toList()
            Collections.endHttpCall(200)
            return events
        } catch (e: Exception) {
            Collections.endHttpCall(-1)
            throw e
        }
    }
}