package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.fetcher.FetcherCache
import base.boudicca.api.eventcollector.logging.CollectionsFilter
import base.boudicca.model.Event

class EventCollectorDebugger {

    fun setFetcherCache(fetcherCache: FetcherCache): EventCollectorDebugger {
        Fetcher.fetcherCache = fetcherCache
        return this
    }

    fun debug(eventCollector: EventCollector) {
        CollectionsFilter.alsoLog = true
        val collectedEvents = mutableListOf<Event>()
        val scheduler = EventCollectorScheduler(eventSink = { collectedEvents.addAll(it) }, enricherFunction = null)
            .startWebUi()
            .addEventCollector(eventCollector)
        scheduler.runOnce()

        collectedEvents.forEach {
            println(it)
        }
        val fullCollection = Collections.getAllPastCollections()[0]
        println(fullCollection)
        println("debugger collected ${collectedEvents.size} events")
        val errorCount = fullCollection.errorCount + fullCollection.singleCollections.sumOf { it.errorCount }
        if (errorCount != 0) {
            println("found $errorCount errors!")
        }
        val warningCount = fullCollection.warningCount + fullCollection.singleCollections.sumOf { it.warningCount }
        if (warningCount != 0) {
            println("found $warningCount warnings!")
        }

        readlnOrNull()
        scheduler.close()
    }

}
