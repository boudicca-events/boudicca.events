package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorDebugger

fun main() {
    EventCollectorDebugger()
//        .debug(TechnologiePlauscherlFetcher())
//        .debug(JkuEventFetcher())
//        .debug(PosthofFetcher())
//        .debug(ZuckerfabrikFetcher())
        .debug(PlanetTTCollector())
}