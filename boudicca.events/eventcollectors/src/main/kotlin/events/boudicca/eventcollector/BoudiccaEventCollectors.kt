package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.Configuration
import base.boudicca.api.eventcollector.EventCollectorCoordinatorBuilder
import base.boudicca.springboot.common.OpenTelemetryUtils
import events.boudicca.eventcollector.collectors.AlpenvereinCollector
import events.boudicca.eventcollector.collectors.AntonBrucknerUniversitaetLinzCollector
import events.boudicca.eventcollector.collectors.ArenaWienCollector
import events.boudicca.eventcollector.collectors.BrucknerhausCollector
import events.boudicca.eventcollector.collectors.BurgClamCollector
import events.boudicca.eventcollector.collectors.CCCEventsCollector
import events.boudicca.eventcollector.collectors.CafeTraxlmayrCollector
import events.boudicca.eventcollector.collectors.ChelseaCollector
import events.boudicca.eventcollector.collectors.ClerieDeChaosEventsCollector
import events.boudicca.eventcollector.collectors.EnnsEventsCollector
import events.boudicca.eventcollector.collectors.FemaleCoderCollector
import events.boudicca.eventcollector.collectors.FhLugCollector
import events.boudicca.eventcollector.collectors.FlohmarktCollector
import events.boudicca.eventcollector.collectors.FraeuleinFlorentineCollector
import events.boudicca.eventcollector.collectors.FuerUnsCollector
import events.boudicca.eventcollector.collectors.JkuEventCollector
import events.boudicca.eventcollector.collectors.KapuCollector
import events.boudicca.eventcollector.collectors.KunstuniversitaetLinzCollector
import events.boudicca.eventcollector.collectors.KupfTicketCollector
import events.boudicca.eventcollector.collectors.LandestheaterLinzCollector
import events.boudicca.eventcollector.collectors.LastSpaceCollector
import events.boudicca.eventcollector.collectors.LinzTermineCollector
import events.boudicca.eventcollector.collectors.MetalCornerCollector
import events.boudicca.eventcollector.collectors.MuseumArbeitsweltCollector
import events.boudicca.eventcollector.collectors.OKHVoecklabruckCollector
import events.boudicca.eventcollector.collectors.OOESeniorenbundCollector
import events.boudicca.eventcollector.collectors.OehJkuCollector
import events.boudicca.eventcollector.collectors.OteloLinzCollector
import events.boudicca.eventcollector.collectors.ParlamentCollector
import events.boudicca.eventcollector.collectors.PlanetTTCollector
import events.boudicca.eventcollector.collectors.PosthofCollector
import events.boudicca.eventcollector.collectors.RoedaCollector
import events.boudicca.eventcollector.collectors.SchlachthofCollector
import events.boudicca.eventcollector.collectors.SpinnereiCollector
import events.boudicca.eventcollector.collectors.StadthalleWienCollector
import events.boudicca.eventcollector.collectors.StadtwerkstattCollector
import events.boudicca.eventcollector.collectors.StiftskonzerteCollector
import events.boudicca.eventcollector.collectors.TabakfabrikLinzCollector
import events.boudicca.eventcollector.collectors.TechnologiePlauscherlCollector
import events.boudicca.eventcollector.collectors.TheaterInDerInnenstadtCollector
import events.boudicca.eventcollector.collectors.TheatherPhoenixCollector
import events.boudicca.eventcollector.collectors.ValugCollector
import events.boudicca.eventcollector.collectors.ViperRoomCollector
import events.boudicca.eventcollector.collectors.WissensturmCollector
import events.boudicca.eventcollector.collectors.ZeroxACollector
import events.boudicca.eventcollector.collectors.ZuckerfabrikCollector
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
        .addEventCollector(TheaterInDerInnenstadtCollector())
        .addEventCollector(TabakfabrikLinzCollector())
        .build()

    eventCollectorCoordinator.startWebUi()
    eventCollectorCoordinator.run()
}
