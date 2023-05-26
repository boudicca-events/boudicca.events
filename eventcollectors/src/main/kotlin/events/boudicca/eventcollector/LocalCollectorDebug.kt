package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorDebugger
import events.boudicca.eventcollector.collectors.*

fun main() {
    EventCollectorDebugger()
//        .debug(TechnologiePlauscherlFetcher())
//        .debug(JkuEventFetcher())
//        .debug(PosthofFetcher())
//        .debug(ZuckerfabrikFetcher())
//        .debug(PlanetTTCollector())
//        .debug(BrucknerhausCollector())
        .debug(LinzTermineCollector())
}