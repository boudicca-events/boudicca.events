package events.boudicca.eventcollector.collectors

import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventdb.publisher.EventDbPublisherClient
import base.boudicca.model.Event

class BoudiccaCollector(private val from: String) : EventCollector {
    override fun getName(): String {
        return "boudicca: $from"
    }

    override fun collectEvents(): List<Event> {
        return EventDbPublisherClient(from).getAllEvents().toList()
    }
}