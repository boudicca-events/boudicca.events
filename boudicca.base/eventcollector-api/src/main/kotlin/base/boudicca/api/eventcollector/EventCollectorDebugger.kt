package base.boudicca.api.eventcollector

import base.boudicca.Event
import base.boudicca.api.eventcollector.collections.Collections

class EventCollectorDebugger {

    fun debug(eventCollector: EventCollector) {
        val collectedEvents = mutableListOf<Event>()
        val scheduler = EventCollectorScheduler(eventSink = { collectedEvents.add(it) }, enricherFunction = null)
            .startWebUi()
            .addEventCollector(eventCollector)
        scheduler.runOnce()

        collectedEvents.forEach {
            println(it)
        }
        println(Collections.getAllPastCollections()[0])
        println("debugger collected ${collectedEvents.size} events")
        val errorCount =
            Collections.getAllPastCollections()[0].logLines
                .union(Collections.getAllPastCollections()[0].singleCollections.flatMap { it.logLines })
                .count { it.first }
        if (errorCount != 0) {
            println("found $errorCount errors!")
        }

        readlnOrNull()
        scheduler.close()
    }

}
