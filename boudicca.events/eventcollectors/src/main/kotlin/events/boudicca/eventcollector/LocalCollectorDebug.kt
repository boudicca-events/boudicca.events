package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.debugger.EventCollectorDebugger
import base.boudicca.api.eventcollector.fetcher.FileBackedFetcherCache
import events.boudicca.eventcollector.collectors.*
import java.io.File

/**
 * this allows you to test a EventCollector locally, while giving you some nice benefits:
 * 1) It also starts the local webui at http://localhost:8083/ where you can follow the progress and see all errors in a better way
 * 2) It caches network calls into a file called "fetcher.cache", so all calls to the external server will only be done once, massively increasing speed the next time you run it
 * 3) Also allows you to enable remote or local enricher
 * 4) Also allows you to ingest the data into your local eventdb
 */
fun main() {
    EventCollectorDebugger()
        // this debugger caches all fetcher calls locally to avoid spamming the server when developing.
        // if there are problems with old data or something like that just delete the file and restart the debugger
        .setFetcherCache(FileBackedFetcherCache(File("./fetcher.cache")))

        //enable one of the two lines to also use the online or local enricher
//        .enableEnricher("https://enricher.boudicca.events")
//        .enableEnricher("http://localhost:8085")

        //enable this line to ingest the collected events into the local eventdb (this uses the configuration from the application.properties)
//        .enableIngestion()

        //enable to test remote collectors !!DON'T FORGET TO DISABLE THE FETCHER CACHE!! for example: https://github.com/boudicca-events/remote-collector-samples
//        .debug(RemoteCollectorCollector("http://localhost:8080"))

        //enable/add any collectors you want to test here
        .debug(FamilienkarteEventCollector())
}
