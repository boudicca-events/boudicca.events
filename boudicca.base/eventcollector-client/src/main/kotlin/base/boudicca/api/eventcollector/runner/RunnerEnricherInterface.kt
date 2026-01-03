package base.boudicca.api.eventcollector.runner

import base.boudicca.api.enricher.EnricherClient
import base.boudicca.model.Event
import io.opentelemetry.api.OpenTelemetry

fun interface RunnerEnricherInterface {
    companion object {
        fun createFromConfiguration(
            otel: OpenTelemetry = OpenTelemetry.noop(),
            enricherUrl: String?,
        ): RunnerEnricherInterface {
            if (enricherUrl.isNullOrBlank()) {
                return NoopRunnerEnricher
            }
            val enricher = EnricherClient(enricherUrl, otel)
            return BoudiccaRunnerEnricherInterface(enricher)
        }
    }

    fun enrichEvents(events: List<Event>): List<Event>
}
