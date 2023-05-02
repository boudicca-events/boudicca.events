package events.boudicca.api.eventcollector

import events.boudicca.openapi.model.Event

interface EventCollector {
    fun getName(): String
    fun collectEvents(): List<Event>
}
