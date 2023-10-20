package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectorScheduler
import events.boudicca.eventcollector.collectors.*

fun main() {
    Thread.sleep(10000) // let eventdb startup first when both are deployed.... we should do a better thing here
    EventCollectorScheduler()
        .startWebUi()
        .addEventCollector(LinzTermineCollector())
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
        .addEventCollector(LandestheaterLinzCollector())
        .addEventCollector(KapuCollector())
        .addEventCollector(StadtwerkstattCollector())
        .addEventCollector(InnovationsHauptplatzCodingWeeksCollector())
        .addEventCollector(OteloLinzCollector())
        .addEventCollector(EnnsEventsCollector())
        .addEventCollector(UlfOoeCollector())
        .addEventCollector(StiftskonzerteCollector())
        .addEventCollector(GewaexhausCollector())
        .run()
}