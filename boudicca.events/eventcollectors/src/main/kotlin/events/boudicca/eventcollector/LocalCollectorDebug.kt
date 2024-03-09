package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectorDebugger
import base.boudicca.api.eventcollector.fetcher.FileBackedFetcherCache
import events.boudicca.eventcollector.collectors.AlpenverreinCollector
import java.io.File

fun main() {
    EventCollectorDebugger()
        // this debugger caches all fetcher calls locally to avoid spamming the server when developing.
        // if there are problems with old data or something like that just delete the file and restart the debugger
        .setFetcherCache(FileBackedFetcherCache(File("./fetcher.cache")))
//        .debug(TechnologiePlauscherlCollector())
//        .debug(JkuEventCollector())
//        .debug(PosthofCollector())
//        .debug(ZuckerfabrikCollector())
//        .debug(PlanetTTCollector())
//        .debug(BrucknerhausCollector())
//        .debug(LinzTermineCollector())
//        .debug(OOESeniorenbundCollector())
//        .debug(KupfTicketCollector())
//        .debug(SpinnereiCollector())
//        .debug(SchlachthofCollector())
//        .debug(WissensturmCollector())
//        .debug(LandestheaterLinzCollector())
//        .debug(KapuCollector())
//        .debug(StadtwerkstattCollector())
//        .debug(OteloLinzCollector())
//        .debug(EnnsEventsCollector())
//        .debug(UlfOoeCollector())
//        .debug(StiftskonzerteCollector())
//        .debug(OehJkuCollector())
//        .debug(ArenaWienCollector())
//        .debug(ViperRoomCollector())
//        .debug(CafeTraxlmayrCollector())
//        .debug(BurgClamCollector())
//        .debug(StadthalleWienCollector())
//        .debug(MuseumArbeitsweltCollector())
//        .debug(OKHVoecklabruckCollector())
//        .debug(ValugCollector())
        .debug(AlpenverreinCollector())
}
