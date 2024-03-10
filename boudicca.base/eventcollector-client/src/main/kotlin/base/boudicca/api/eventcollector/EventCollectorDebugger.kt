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
        val fullCollection = Collections.getLastFullCollection()
        println(fullCollection)
        println("debugger collected ${collectedEvents.size} events")
        val errorCount = fullCollection.getTotalErrorCount()
        if (errorCount != 0) {
            println("found $errorCount errors!")
        }
        val warningCount = fullCollection.getTotalWarningCount()
        if (warningCount != 0) {
            println("found $warningCount warnings!")
        }

        readlnOrNull()
        scheduler.close()
    }

}
