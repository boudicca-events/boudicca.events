package events.boudicca.api.eventcollector

class EventCollectorDebugger {

    fun debug(eventCollector: EventCollector) {
        val events = eventCollector.collectEvents()
        println("debugger collected ${events.size} events: ")
        //TODO validate?
        events.forEach {
            println(it)
        }
    }

}
