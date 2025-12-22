package base.boudicca.api.eventcollector.collectors

import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventdb.publisher.EventDbPublisherClient
import base.boudicca.model.Event
import java.net.HttpURLConnection

/**
 * EventCollector implementation which will collect events from a Boudicca instance.
 * useful for federation purposes or for local development to get the data from an online Boudicca instance.
 */
class BoudiccaCollector(private val url: String, private val name: String? = null) : EventCollector {
    override fun getName(): String = name ?: "boudicca: $url"

    override fun collectEvents(): List<Event> {
        Collections.startHttpCall(url)
        try {
            val events = EventDbPublisherClient(url).getAllEvents().toList()
            Collections.endHttpCall(HttpURLConnection.HTTP_OK)
            return events
        } catch (e: Exception) {
            Collections.endHttpCall(-1)
            throw e
        }
    }
}
