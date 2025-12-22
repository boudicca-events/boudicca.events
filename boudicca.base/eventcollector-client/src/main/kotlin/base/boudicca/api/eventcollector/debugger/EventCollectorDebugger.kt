package base.boudicca.api.eventcollector.debugger

import base.boudicca.SemanticKeys
import base.boudicca.api.enricher.EnricherClient
import base.boudicca.api.eventcollector.Configuration
import base.boudicca.api.eventcollector.EventCollectionRunner
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.EventCollectorWebUi
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.debugger.color.green
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.api.eventcollector.logging.CollectionsFilter
import base.boudicca.api.eventcollector.runner.BoudiccaRunnerEnricherInterface
import base.boudicca.api.eventcollector.runner.BoudiccaRunnerIngestionInterface
import base.boudicca.api.eventcollector.runner.NoopRunnerEnricher
import base.boudicca.api.eventcollector.runner.RunnerEnricherInterface
import base.boudicca.api.eventcollector.runner.RunnerIngestionInterface
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.fetcher.FetcherCache
import base.boudicca.model.Event
import io.opentelemetry.api.OpenTelemetry

private const val DEFAULT_PORT = 8083

class EventCollectorDebugger(val verboseDebugging: Boolean = true, val verboseValidation: Boolean = true, val startWebUi: Boolean = true) {
    init {
        CollectionsFilter.alsoLog = true
        FetcherFactory.disableRetries = true
    }

    private var runnerIngestionInterface: RunnerIngestionInterface? = null
    private var runnerEnricherInterface: RunnerEnricherInterface? = null
    private val allEvents = mutableListOf<Event>()

    fun setFetcherCache(fetcherCache: FetcherCache): EventCollectorDebugger {
        FetcherFactory.defaultFetcherCache = fetcherCache
        return this
    }

    fun enableIngestion(): EventCollectorDebugger = enableIngestion(RunnerIngestionInterface.createFromConfiguration())

    fun enableIngestion(eventDbUrl: String, user: String, password: String): EventCollectorDebugger = enableIngestion(EventDbIngestClient(eventDbUrl, user, password))

    fun enableIngestion(ingestClient: EventDbIngestClient): EventCollectorDebugger = enableIngestion(BoudiccaRunnerIngestionInterface(ingestClient))

    fun enableIngestion(runnerIngestionInterface: RunnerIngestionInterface): EventCollectorDebugger {
        this.runnerIngestionInterface = runnerIngestionInterface
        return this
    }

    fun enableEnricher(): EventCollectorDebugger = enableEnricher(RunnerEnricherInterface.createFromConfiguration())

    fun enableEnricher(enricherUrl: String): EventCollectorDebugger = enableEnricher(EnricherClient(enricherUrl))

    fun enableEnricher(enricherClient: EnricherClient): EventCollectorDebugger = enableEnricher(BoudiccaRunnerEnricherInterface(enricherClient))

    fun enableEnricher(runnerEnricherInterface: RunnerEnricherInterface): EventCollectorDebugger {
        this.runnerEnricherInterface = runnerEnricherInterface
        return this
    }

    fun debug(eventCollector: EventCollector): EventCollectorDebugger {
        val eventCollectorAsList = listOf(eventCollector)
        val collectedEvents = mutableListOf<Event>()

        val configuredWebUiPort = Configuration.getProperty("server.port")?.toInt() ?: DEFAULT_PORT
        var eventCollectorWebUi: EventCollectorWebUi? = null
        if (startWebUi) {
            eventCollectorWebUi =
                EventCollectorWebUi(
                    configuredWebUiPort,
                    eventCollectorAsList,
                    OpenTelemetry.noop(),
                )
            eventCollectorWebUi.start()
        }

        val runner =
            EventCollectionRunner(
                eventCollectorAsList,
                {
                    collectedEvents.addAll(it)
                    if (runnerIngestionInterface != null) {
                        runnerIngestionInterface!!.ingestEvents(it)
                    }
                },
                runnerEnricherInterface ?: NoopRunnerEnricher,
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

        if (eventCollectorWebUi != null) {
            println("webui is running on http://localhost:$configuredWebUiPort and is blocking until you press enter in this console")
            readlnOrNull()
            eventCollectorWebUi.stop()
        }

        return this
    }

    fun validate(validations: List<EventCollectorValidation>): EventCollectorDebugger {
        allEvents.forEach { event ->
            println("=========================================================================")
            println("${event.data[SemanticKeys.COLLECTORNAME]} - ${event.startDate} ${event.name}")
            val highestSeverity =
                validations.minOfOrNull { validation ->
                    validation.validate(event, verboseValidation)
                }
            if (highestSeverity == ValidationResult.Error) {
                println("Validation: CHECK FAILED".red())
            } else if (highestSeverity == ValidationResult.Warn) {
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
