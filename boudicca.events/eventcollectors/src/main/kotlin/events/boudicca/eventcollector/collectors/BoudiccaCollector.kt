package events.boudicca.eventcollector.collectors

import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.EventCollector
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventPublisherResourceApi

class BoudiccaCollector(private val from: String) : EventCollector {
    override fun getName(): String {
        return "boudicca: $from"
    }

    override fun collectEvents(): List<Event> {
        val apiClient = ApiClient()
        apiClient.updateBaseUri(from)
        val publisherApi = EventPublisherResourceApi(apiClient)
        return publisherApi.eventsGet().map { Event(it.name, it.startDate, it.data!!) }
    }
}