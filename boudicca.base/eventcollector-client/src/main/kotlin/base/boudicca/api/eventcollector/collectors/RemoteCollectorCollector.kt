package base.boudicca.api.eventcollector.collectors

import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.remotecollector.RemoteCollectorClient
import base.boudicca.model.Event

class RemoteCollectorCollector(private val url: String, private val name: String? = null) : EventCollector {
    override fun getName(): String {
        return name ?: "remote collector: $url"
    }

    override fun collectEvents(): List<Event> {
        Collections.startHttpCall(url)
        val eventCollection = try {
            val eventCollection = RemoteCollectorClient(url).collectEvents()
            Collections.endHttpCall(200)
            eventCollection
        } catch (e: Exception) {
            Collections.endHttpCall(-1)
            throw e
        }

        //TODO use returned meta information somehow

        return eventCollection.events
    }
}