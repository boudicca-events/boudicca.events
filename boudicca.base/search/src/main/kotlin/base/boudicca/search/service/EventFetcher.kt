package base.boudicca.search.service

import base.boudicca.api.eventdb.publisher.EventDbPublisherClient
import base.boudicca.model.Entry
import base.boudicca.search.BoudiccaSearchProperties
import io.opentelemetry.api.OpenTelemetry
import org.springframework.stereotype.Service

@Service
class EventDBEventFetcher(
    private val boudiccaSearchProperties: BoudiccaSearchProperties,
    private val otel: OpenTelemetry,
) : EventFetcher {
    private val publisherApi: EventDbPublisherClient = createEventPublisherApi()

    override fun fetchAllEvents(): Set<Entry> {
        return publisherApi.getAllEntries()
    }

    private fun createEventPublisherApi(): EventDbPublisherClient {
        return EventDbPublisherClient(boudiccaSearchProperties.eventDB.url, otel)
    }
}

fun interface EventFetcher {
    fun fetchAllEvents(): Set<Entry>
}
