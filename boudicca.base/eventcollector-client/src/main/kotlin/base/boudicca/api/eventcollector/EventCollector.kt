package base.boudicca.api.eventcollector

import base.boudicca.model.Event

interface EventCollector {
    fun getName(): String
    fun collectEvents(): List<Event>
}
