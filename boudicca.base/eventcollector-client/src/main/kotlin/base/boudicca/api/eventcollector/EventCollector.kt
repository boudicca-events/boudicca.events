package base.boudicca.api.eventcollector

import base.boudicca.model.Event
import base.boudicca.model.structured.StructuredEvent

interface EventCollector {
    fun getName(): String

    fun collectEvents(): List<Event> {
        return collectStructuredEvents()
            .map { it.toFlatEvent() }
    }

    fun collectStructuredEvents(): List<StructuredEvent> {
        return emptyList()
    }
}
