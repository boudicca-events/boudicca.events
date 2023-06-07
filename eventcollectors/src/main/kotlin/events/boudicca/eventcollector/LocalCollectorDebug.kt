package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorDebugger
import events.boudicca.eventcollector.collectors.*

fun main() {
    EventCollectorDebugger()
//        .debug(TechnologiePlauscherlCollector())
//        .debug(JkuEventCollector())
//        .debug(PosthofCollector())
//        .debug(ZuckerfabrikCollector())
//        .debug(PlanetTTCollector())
//        .debug(BrucknerhausCollector())
//        .debug(LinzTermineCollector())
//        .debug(OOESeniorenbundCollector())
//        .debug(KupfTicketCollector())
        .debug(SpinnereiCollector())
}