package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorScheduler
import events.boudicca.api.eventcollector.collections.Collections
import events.boudicca.eventcollector.collectors.BoudiccaCollector

fun main() {
    EventCollectorScheduler()
        .addEventCollector(BoudiccaCollector("https://eventdb.boudicca.events"))
        .runOnce()
    Collections.getAllPastCollections()[0].logLines +
            Collections.getAllPastCollections()[0].singleCollections.flatMap { it.logLines }
                .forEach { println(String(it.second)) }
}