package base.boudicca.api.eventcollector.debugger

import base.boudicca.SemanticKeys
import base.boudicca.api.enricher.EnricherClient
import base.boudicca.api.eventcollector.*
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.debugger.color.green
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.fetcher.FetcherCache
import base.boudicca.api.eventcollector.logging.CollectionsFilter
import base.boudicca.api.eventcollector.runner.*
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.model.Event

class EventCollectorDebugger(val verboseDebugging: Boolean = true,
                             val verboseValidation: Boolean = true,
                             val keepOpen: Boolean = true) {

    private var runnerIngestionInterface: RunnerIngestionInterface? = null
    private var runnerEnricherInterface: RunnerEnricherInterface? = null
    private val allEvents = mutableListOf<Event>()

    fun setFetcherCache(fetcherCache: FetcherCache): EventCollectorDebugger {
        FetcherFactory.defaultFetcherCache = fetcherCache
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

    fun debug(eventCollector: EventCollector): EventCollectorDebugger {
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

        if (verboseDebugging) {
            collectedEvents.forEach {
                println(it)
            }
        }
        allEvents.addAll(collectedEvents)
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

        if (keepOpen) {
            readlnOrNull()
        }
        eventCollectorWebUi.stop()

        return this
    }

    fun validate(
        validations: List<EventCollectorValidation>
    ): EventCollectorDebugger {
        allEvents.forEach { event ->
            println("=========================================================================")
            println("${event.data.get(SemanticKeys.COLLECTORNAME)} - ${event.startDate} ${event.name}")
            val highestSeverity = validations.minOfOrNull { validation ->
                validation.validate(event, verboseValidation)
            }
            if (highestSeverity == ValidationResult.Error) {
                println("Validation: CHECK FAILED".red())
            } else if (highestSeverity == ValidationResult.Warn)  {
                println("Validation: CHECK WARN".yellow())
            } else {
                println("Validation: CHECK OK".green())
            }
            println()
            println()
        }
        return this
    }

    fun clearEvents(): EventCollectorDebugger {
        allEvents.clear()
        return this
    }

}
