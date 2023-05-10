package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorScheduler

fun main() {
    EventCollectorScheduler()
        .addEventCollector(PosthofFetcher())
        .addEventCollector(JkuEventFetcher())
        .addEventCollector(TechnologiePlauscherlFetcher())
        .addEventCollector(ZuckerfabrikFetcher())
        .addEventCollector(PlanetTTCollector())
        .run()
}