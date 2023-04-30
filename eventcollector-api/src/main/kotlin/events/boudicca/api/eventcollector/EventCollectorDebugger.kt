package events.boudicca.api.eventcollector

class EventCollectorDebugger {

    fun debug(eventCollector: EventCollector) {
        val events = eventCollector.collectEvents()
        //TODO validate?
        events.forEach {
            println(it)
        }
    }

}
