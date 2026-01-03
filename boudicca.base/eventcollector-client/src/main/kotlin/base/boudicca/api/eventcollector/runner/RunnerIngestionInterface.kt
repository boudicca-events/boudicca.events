package base.boudicca.api.eventcollector.runner

import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.model.Event
import io.opentelemetry.api.OpenTelemetry

fun interface RunnerIngestionInterface {
    companion object {
        fun createFromConfiguration(
            otel: OpenTelemetry = OpenTelemetry.noop(),
            eventDbUrl: String?,
            ingestAuth: String?,
        ): RunnerIngestionInterface {
            require(!eventDbUrl.isNullOrBlank()) { "you need to specify the boudicca.eventdb.url property!" }
            requireNotNull(ingestAuth) { "you need to specify the boudicca.ingest.auth property!" }
            val user = ingestAuth.split(":")[0]
            val password = ingestAuth.split(":")[1]
            val eventDb = EventDbIngestClient(eventDbUrl, user, password, otel)
            return BoudiccaRunnerIngestionInterface(eventDb)
        }
    }

    fun ingestEvents(events: List<Event>)
}
