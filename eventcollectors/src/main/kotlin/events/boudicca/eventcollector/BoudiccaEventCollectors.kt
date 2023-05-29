package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorScheduler
import events.boudicca.eventcollector.collectors.*

fun main() {
    EventCollectorScheduler()
        .addEventCollector(PosthofCollector())
        .addEventCollector(JkuEventCollector())
        .addEventCollector(TechnologiePlauscherlCollector())
        .addEventCollector(ZuckerfabrikCollector())
        .addEventCollector(PlanetTTCollector())
        .addEventCollector(BrucknerhausCollector())
        .addEventCollector(LinzTermineCollector())
        .addEventCollector(OOESeniorenbundCollector())
        .addEventCollector(KupfTicketCollector())
        .run()
}