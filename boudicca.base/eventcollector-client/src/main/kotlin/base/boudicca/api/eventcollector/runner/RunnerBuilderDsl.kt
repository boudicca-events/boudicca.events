package base.boudicca.api.eventcollector.runner

import base.boudicca.api.enricher.EnricherClient
import base.boudicca.api.eventcollector.EventCollectionRunner
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.fetcher.FetcherCache
import base.boudicca.model.Event
import io.opentelemetry.api.OpenTelemetry

fun buildRunnerFor(eventCollectors: List<EventCollector>) = RunnerBuilderDsl(eventCollectors)

class RunnerBuilderDsl(
    private val eventCollectors: List<EventCollector>,
) {
    private var runnerIngestionInterface: RunnerIngestionInterface? = null
    private var runnerEnricherInterface: RunnerEnricherInterface? = null
    private var otel: OpenTelemetry = OpenTelemetry.noop()
    private var captureHttpCalls: Boolean = false

    fun withFetcherCache(fetcherCache: FetcherCache): RunnerBuilderDsl {
        FetcherFactory.defaultFetcherCache = fetcherCache
        return this
    }

    fun withOtel(openTelemetry: OpenTelemetry) {
        this.otel = openTelemetry
    }

    fun withIngestion(
        eventDbUrl: String,
        user: String,
        password: String,
    ): RunnerBuilderDsl = withIngestion(EventDbIngestClient(eventDbUrl, user, password))

    fun withIngestion(ingestClient: EventDbIngestClient): RunnerBuilderDsl = withIngestion(BoudiccaRunnerIngestionInterface(ingestClient))

    fun withIngestion(runnerIngestionInterface: RunnerIngestionInterface): RunnerBuilderDsl {
        this.runnerIngestionInterface = runnerIngestionInterface
        return this
    }

    fun withEnricher(enricherUrl: String): RunnerBuilderDsl = withEnricher(EnricherClient(enricherUrl))

    fun withEnricher(enricherClient: EnricherClient): RunnerBuilderDsl = withEnricher(BoudiccaRunnerEnricherInterface(enricherClient))

    fun withEnricher(runnerEnricherInterface: RunnerEnricherInterface): RunnerBuilderDsl {
        this.runnerEnricherInterface = runnerEnricherInterface
        return this
    }

    fun captureHttpCalls(captureHttpCalls: Boolean = true) {
        this.captureHttpCalls = captureHttpCalls
    }

    fun run() {
        val runner =
            EventCollectionRunner(
                otel,
                captureHttpCalls,
            )
        runner.run(
            eventCollectors,
            runnerIngestionInterface ?: NoopRunnerIngestion,
            runnerEnricherInterface ?: NoopRunnerEnricher,
        )
    }

    fun buildDebugRunner(): DebugRunner =
        DebugRunner(
            EventCollectionRunner(
                otel,
                captureHttpCalls,
            ),
            eventCollectors,
            runnerIngestionInterface,
            runnerEnricherInterface,
        )
}

class DebugRunner(
    val runner: EventCollectionRunner,
    val eventCollectors: List<EventCollector>,
    val runnerIngestionInterface: RunnerIngestionInterface?,
    val runnerEnricherInterface: RunnerEnricherInterface?,
) {
    fun run(): List<Event> {
        val collectedEvents = mutableListOf<Event>()

        runner.run(
            eventCollectors,
            {
                val ingestion = runnerIngestionInterface
                collectedEvents.addAll(it)
                ingestion?.ingestEvents(it)
            },
            runnerEnricherInterface ?: NoopRunnerEnricher,
        )

        return collectedEvents
    }
}
