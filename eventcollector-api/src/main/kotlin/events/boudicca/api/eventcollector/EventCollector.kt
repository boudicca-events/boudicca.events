package events.boudicca.api.eventcollector

interface EventCollector {
    fun getName(): String
    fun collectEvents(): List<Event>
}
