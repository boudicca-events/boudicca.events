package events.boudicca.api.eventcollector

import events.boudicca.api.eventcollector.collections.Collections

class EventCollectorDebugger {

    fun debug(eventCollector: EventCollector) {
        Collections.startFullCollection()
        Collections.startSingleCollection(eventCollector)
        val events = eventCollector.collectEvents()
        Collections.endSingleCollection()
        Collections.endFullCollection()
        events.forEach {
            println(it)
        }
        println(Collections.getAllPastCollections()[0])
        println("debugger collected ${events.size} events")
    }

}
