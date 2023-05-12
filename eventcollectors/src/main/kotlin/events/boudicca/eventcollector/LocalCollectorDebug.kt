package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorDebugger
import events.boudicca.eventcollector.collectors.BrucknerhausCollector
import events.boudicca.eventcollector.collectors.PlanetTTCollector

fun main() {
    EventCollectorDebugger()
//        .debug(TechnologiePlauscherlFetcher())
//        .debug(JkuEventFetcher())
//        .debug(PosthofFetcher())
//        .debug(ZuckerfabrikFetcher())
//        .debug(PlanetTTCollector())
        .debug(BrucknerhausCollector())
}