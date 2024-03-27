package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectorCoordinatorBuilder
import base.boudicca.api.eventcollector.collectors.BoudiccaCollector
import events.boudicca.eventcollector.collectors.*

fun main() {
    val eventCollectorCoordinator = EventCollectorCoordinatorBuilder()
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
        .addEventCollector(OteloLinzCollector())
        .addEventCollector(EnnsEventsCollector())
        .addEventCollector(UlfOoeCollector())
        .addEventCollector(StiftskonzerteCollector())
        .addEventCollector(OehJkuCollector())
        .addEventCollector(ArenaWienCollector())
        .addEventCollector(ViperRoomCollector())
        .addEventCollector(CafeTraxlmayrCollector())
        .addEventCollector(BurgClamCollector())
        .addEventCollector(StadthalleWienCollector())
        .addEventCollector(MuseumArbeitsweltCollector())
        .addEventCollector(OKHVoecklabruckCollector())
        .addEventCollector(ValugCollector())
        .addEventCollector(AlpenvereinCollector())
        .addEventCollector(BoudiccaCollector("https://museumsbahn-events.at", "museumsbahnen"))
        .build()

    eventCollectorCoordinator.startWebUi()
    eventCollectorCoordinator.run()
}