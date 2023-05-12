package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorScheduler
import events.boudicca.eventcollector.collectors.BoudiccaCollector

fun main() {
    EventCollectorScheduler()
        .addEventCollector(BoudiccaCollector("https://api.boudicca.events"))
        .runOnce()
}