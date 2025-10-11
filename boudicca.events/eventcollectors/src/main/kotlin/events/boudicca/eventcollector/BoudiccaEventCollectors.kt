package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.Configuration
import base.boudicca.api.eventcollector.EventCollectorCoordinatorBuilder
import base.boudicca.springboot.common.OpenTelemetryUtils
import events.boudicca.eventcollector.collectors.*
import io.opentelemetry.api.GlobalOpenTelemetry

fun main() {
    val otel = OpenTelemetryUtils.createOpenTelemetry(
        Configuration.getProperty("boudicca.monitoring.endpoint"),
        Configuration.getProperty("boudicca.monitoring.user"),
        Configuration.getProperty("boudicca.monitoring.password"),
        "collectors",
    )
    GlobalOpenTelemetry.set(otel)

    val eventCollectorCoordinator = EventCollectorCoordinatorBuilder(otel)
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
        .addEventCollector(FuerUnsCollector())
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
        .addEventCollector(MetalCornerCollector())
        .addEventCollector(FemaleCoderCollector())
        .addEventCollector(FhLugCollector())
        .addEventCollector(ZeroxACollector())
        .addEventCollector(CCCEventsCollector())
        .addEventCollector(ClerieDeChaosEventsCollector())
//        .addEventCollector(FamilienkarteEventCollector())
        .addEventCollector(KunstuniversitaetLinzCollector())
        .addEventCollector(AntonBrucknerUniversitaetLinzCollector())
        .addEventCollector(TheatherPhoenixCollector())
        .addEventCollector(RoedaCollector())
        .addEventCollector(ParlamentCollector())
        .addEventCollector(FlohmarktCollector())
        .addEventCollector(FraeuleinFlorentineCollector())
        .addEventCollector(ChelseaCollector())
        .addEventCollector(LastSpaceCollector())
        .build()

    eventCollectorCoordinator.startWebUi()
    eventCollectorCoordinator.run()
}
