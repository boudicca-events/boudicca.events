package base.boudicca.search.service

import base.boudicca.api.eventdb.publisher.EventDbPublisherClient
import base.boudicca.model.Entry
import base.boudicca.search.BoudiccaSearchProperties
import org.springframework.stereotype.Service

@Service
class EventDBEventFetcher(
    private val boudiccaSearchProperties: BoudiccaSearchProperties
) : EventFetcher {

    private val publisherApi: EventDbPublisherClient = createEventPublisherApi()

    override fun fetchAllEvents(): Set<Entry> {
        return publisherApi.getAllEntries()
    }

    private fun createEventPublisherApi(): EventDbPublisherClient {
        return EventDbPublisherClient(boudiccaSearchProperties.eventDB.url)
    }
}

interface EventFetcher {
    fun fetchAllEvents(): Set<Entry>
}
