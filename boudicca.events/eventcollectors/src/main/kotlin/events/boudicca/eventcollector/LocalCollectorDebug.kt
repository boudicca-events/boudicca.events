package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectorDebugger
import base.boudicca.api.eventcollector.collectors.BoudiccaCollector
import base.boudicca.api.eventcollector.fetcher.FileBackedFetcherCache
import events.boudicca.eventcollector.collectors.*
import java.io.File

fun main() {
    EventCollectorDebugger()
        // this debugger caches all fetcher calls locally to avoid spamming the server when developing.
        // if there are problems with old data or something like that just delete the file and restart the debugger
        .setFetcherCache(FileBackedFetcherCache(File("./fetcher.cache")))
        //enable one of the two lines to also use the online or local enricher
//        .enableEnricher("https://enricher.boudicca.events")
//        .enableEnricher("http://localhost:8085")
        //enable this line to ingest the collected events into the local eventdb (this uses the configuration from the application.properties)
        .enableIngestion()

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
//        .debug(AlpenvereinCollector())
        .debug(BoudiccaCollector("https://museumsbahn-events.at", "museumsbahnen"))

}
