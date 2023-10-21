package events.boudicca.eventcollector.collectors

import base.boudicca.Event
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventdb.publisher.EventDB

class BoudiccaCollector(private val from: String) : EventCollector {
    override fun getName(): String {
        return "boudicca: $from"
    }

    override fun collectEvents(): List<Event> {
        return EventDB(from).getAllEvents().toList()
    }
}