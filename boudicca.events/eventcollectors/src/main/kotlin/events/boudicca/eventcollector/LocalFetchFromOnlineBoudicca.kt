package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectorScheduler
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.collectors.BoudiccaCollector

fun main() {
    EventCollectorScheduler()
        .addEventCollector(BoudiccaCollector("https://eventdb.boudicca.events"))
        .runOnce()
    Collections.getAllPastCollections()[0].getAllLogLines().forEach { println(it) }
}