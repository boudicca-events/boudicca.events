package events.boudicca.eventcollector

import events.boudicca.api.eventcollector.EventCollectorScheduler
import events.boudicca.eventcollector.collectors.*

fun main() {
    Thread.sleep(10000) // let core startup first when both are deployed.... we should do a better thing here
    EventCollectorScheduler()
//        .addEventCollector(LinzTermineCollector())
        .addEventCollector(PosthofCollector())
        .addEventCollector(JkuEventCollector())
        .addEventCollector(TechnologiePlauscherlCollector())
        .addEventCollector(ZuckerfabrikCollector())
        .addEventCollector(PlanetTTCollector())
        .addEventCollector(BrucknerhausCollector())
        .addEventCollector(OOESeniorenbundCollector())
        .addEventCollector(KupfTicketCollector())
        .addEventCollector(SpinnereiCollector())
        .addEventCollector(SchlachthofCollector())
        .addEventCollector(WissensturmCollector())
        .run()
}