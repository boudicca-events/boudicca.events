package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorScheduler
import events.boudicca.eventcollector.collectors.*

fun main() {
    EventCollectorScheduler()
        .addEventCollector(PosthofFetcher())
        .addEventCollector(JkuEventFetcher())
        .addEventCollector(TechnologiePlauscherlFetcher())
        .addEventCollector(ZuckerfabrikFetcher())
        .addEventCollector(PlanetTTCollector())
        .addEventCollector(BrucknerhausCollector())
        .run()
}