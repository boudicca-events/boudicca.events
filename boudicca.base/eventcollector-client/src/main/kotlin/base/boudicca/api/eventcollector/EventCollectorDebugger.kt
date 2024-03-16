package base.boudicca.api.eventcollector

import base.boudicca.api.enricher.EnricherClient
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.fetcher.FetcherCache
import base.boudicca.api.eventcollector.logging.CollectionsFilter
import base.boudicca.api.eventcollector.runner.*
import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.model.Event

class EventCollectorDebugger {

    private var runnerIngestionInterface: RunnerIngestionInterface? = null
    private var runnerEnricherInterface: RunnerEnricherInterface? = null

    fun setFetcherCache(fetcherCache: FetcherCache): EventCollectorDebugger {
        Fetcher.fetcherCache = fetcherCache
        return this
    }

    fun enableIngestion(): EventCollectorDebugger {
        return enableIngestion(RunnerIngestionInterface.createFromConfiguration())
    }

    fun enableIngestion(eventDbUrl: String, user: String, password: String): EventCollectorDebugger {
        return enableIngestion(EventDbIngestClient(eventDbUrl, user, password))
    }

    fun enableIngestion(ingestClient: EventDbIngestClient): EventCollectorDebugger {
        return enableIngestion(BoudiccaRunnerIngestionInterface(ingestClient))
    }

    fun enableIngestion(runnerIngestionInterface: RunnerIngestionInterface): EventCollectorDebugger {
        this.runnerIngestionInterface = runnerIngestionInterface
        return this
    }

    fun enableEnricher(): EventCollectorDebugger {
        return enableEnricher(RunnerEnricherInterface.createFromConfiguration())
    }

    fun enableEnricher(enricherUrl: String): EventCollectorDebugger {
        return enableEnricher(EnricherClient(enricherUrl))
    }

    fun enableEnricher(enricherClient: EnricherClient): EventCollectorDebugger {
        return enableEnricher(BoudiccaRunnerEnricherInterface(enricherClient))
    }

    fun enableEnricher(runnerEnricherInterface: RunnerEnricherInterface): EventCollectorDebugger {
        this.runnerEnricherInterface = runnerEnricherInterface
        return this
    }

    fun debug(eventCollector: EventCollector) {
        CollectionsFilter.alsoLog = true
        val eventCollectorAsList = listOf(eventCollector)
        val collectedEvents = mutableListOf<Event>()

        val eventCollectorWebUi =
            EventCollectorWebUi(Configuration.getProperty("server.port")?.toInt() ?: 8083, eventCollectorAsList)
        eventCollectorWebUi.start()

        val runner = EventCollectionRunner(
            eventCollectorAsList,
            {
                collectedEvents.addAll(it)
                if (runnerIngestionInterface != null) {
                    runnerIngestionInterface!!.ingestEvents(it)
                }
            },
            runnerEnricherInterface ?: NoopRunnerEnricherInterface
        )
        runner.run()

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
        eventCollectorWebUi.stop()
    }

}
