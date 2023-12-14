package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectorScheduler
import events.boudicca.eventcollector.collectors.*

fun main() {
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
        .addEventCollector(OehJkuCollector())
        .addEventCollector(ArenaWienCollector())
        .addEventCollector(ViperRoomCollector())
        .addEventCollector(CafeTraxlmayrCollector())
        .addEventCollector(BurgClamCollector())
        .addEventCollector(StadthalleWienCollector())
        .addEventCollector(MuseumArbeitsweltCollector())
        .addEventCollector(OKHVoecklabruckCollector())
        .run()
}