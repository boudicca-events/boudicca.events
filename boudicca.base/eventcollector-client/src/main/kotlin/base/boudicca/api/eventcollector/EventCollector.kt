package base.boudicca.api.eventcollector

import base.boudicca.Event

interface EventCollector {
    fun getName(): String
    fun collectEvents(): List<Event>
}
